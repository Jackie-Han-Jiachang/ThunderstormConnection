import {api} from "./api.js";
import {renderGraph} from "./graph.js";

const state = {events: [], characters: [], graph: {nodes: [], links: []}};
const $ = selector => document.querySelector(selector);
const dialog = $("#event-dialog");
let stopGraph;

document.addEventListener("DOMContentLoaded", initialize);

async function initialize() {
    bindActions();
    try {
        [state.events, state.characters, state.graph] = await Promise.all([
            api.getEvents(), api.getCharacters(), api.getGraph()
        ]);
        buildCharacterFields();
        render();
    } catch (error) {
        showToast(error.message || "应用数据加载失败");
    } finally {
        $(".workspace").setAttribute("aria-busy", "false");
    }
}

function bindActions() {
    $("#create-event").addEventListener("click", () => openForm());
    $("#close-dialog").addEventListener("click", closeForm);
    $("#cancel-event").addEventListener("click", closeForm);
    $("#event-description").addEventListener("input", updateDescriptionCount);
    $("#event-affection").addEventListener("input", updateAffection);
    $("#event-initiator").addEventListener("change", updateAffectedAvailability);
    $("#event-form").addEventListener("submit", saveEvent);
    $("#prepared-list").addEventListener("click", handleCardAction);
    $("#occurred-list").addEventListener("click", handleCardAction);
    window.addEventListener("resize", debounce(() => renderGraphNow(), 180));
}

function buildCharacterFields() {
    const select = $("#event-initiator");
    const options = $("#affected-options");
    state.characters.forEach(character => {
        select.add(new Option(character.name, character.id));
        const label = document.createElement("label");
        label.className = "character-option";
        label.innerHTML = `<input type="checkbox" name="affectedCharacters" value="${character.id}"><span>${character.name}</span>`;
        options.append(label);
    });
}

function render() {
    const prepared = state.events.filter(event => event.status === "PREPARED");
    const occurred = state.events.filter(event => event.status === "OCCURRED").reverse();
    $("#prepared-count").textContent = prepared.length;
    $("#occurred-count").textContent = occurred.length;
    renderEventList($("#prepared-list"), prepared, true);
    renderEventList($("#occurred-list"), occurred, false);
    renderGraphNow();
}

function renderEventList(container, events, prepared) {
    if (!events.length) {
        container.innerHTML = `<div class="list-empty">${prepared ? "还没有预备事件，创建一张事件卡片吧。" : "尚无已发生事件。"}</div>`;
        return;
    }
    container.replaceChildren(...events.map(event => eventCard(event, prepared)));
}

function eventCard(event, prepared) {
    const article = document.createElement("article");
    const scoreClass = tone(event.affectionDelta);
    article.className = `event-card ${scoreClass}`;
    article.dataset.id = event.id;
    const affected = [...event.affectedCharacters].map(characterName).join("、");
    const score = event.affectionDelta > 0 ? `+${event.affectionDelta}` : `${event.affectionDelta}`;
    article.innerHTML = `
        <div class="event-topline">
            <h3>${escapeHtml(event.name)}</h3>
            <span class="score-badge ${scoreClass}" aria-label="好感度变化 ${score}">${score}</span>
        </div>
        <p>${escapeHtml(event.description)}</p>
        <dl class="event-details">
            <dt>发起人</dt><dd>${characterName(event.initiator)}</dd>
            <dt>被影响人</dt><dd>${affected}</dd>
        </dl>
        <div class="card-actions">
            ${prepared
                ? `<button data-action="edit">编辑</button><button data-action="delete">删除</button><button class="perform" data-action="activate">执行事件 →</button>`
                : `<button data-action="revert">↩ 撤回到预备事件</button>`}
        </div>`;
    return article;
}

async function handleCardAction(event) {
    const button = event.target.closest("button[data-action]");
    if (!button) return;
    const id = button.closest(".event-card").dataset.id;
    const action = button.dataset.action;
    const item = state.events.find(current => current.id === id);
    if (action === "edit") return openForm(item);
    if (action === "delete" && !confirm(`确定删除“${item.name}”吗？`)) return;
    button.disabled = true;
    try {
        if (action === "delete") await api.deleteEvent(id);
        if (action === "activate") await api.activateEvent(id);
        if (action === "revert") await api.revertEvent(id);
        await refresh();
        showToast(action === "activate" ? "事件已登上舞台，关系网已更新" : action === "revert" ? "事件已撤回预备区" : "事件已删除");
    } catch (error) {
        showToast(error.message || "操作失败");
        button.disabled = false;
    }
}

function openForm(event) {
    $("#event-form").reset();
    clearErrors();
    $("#event-id").value = event?.id || "";
    $("#dialog-title").textContent = event ? "编辑预备事件" : "创建新事件";
    if (event) {
        $("#event-name").value = event.name;
        $("#event-description").value = event.description;
        $("#event-initiator").value = event.initiator;
        $("#event-affection").value = event.affectionDelta;
        document.querySelectorAll("[name=affectedCharacters]").forEach(input => {
            input.checked = event.affectedCharacters.includes(input.value);
        });
    }
    updateDescriptionCount();
    updateAffection();
    updateAffectedAvailability();
    dialog.showModal();
    $("#event-name").focus();
}

function closeForm() {
    dialog.close();
}

async function saveEvent(event) {
    event.preventDefault();
    clearErrors();
    const data = {
        name: $("#event-name").value.trim(),
        description: $("#event-description").value.trim(),
        initiator: $("#event-initiator").value || null,
        affectedCharacters: [...document.querySelectorAll("[name=affectedCharacters]:checked")].map(input => input.value),
        affectionDelta: Number($("#event-affection").value)
    };
    if (!validate(data)) return;
    const id = $("#event-id").value;
    const submit = event.submitter;
    submit.disabled = true;
    try {
        if (id) await api.updateEvent(id, data);
        else await api.createEvent(data);
        closeForm();
        await refresh();
        showToast(id ? "事件卡片已更新" : "事件已保存到预备区");
    } catch (error) {
        if (error.fields) {
            Object.entries(error.fields).forEach(([field, message]) => setFieldError(field, message));
        }
        $("#form-error").textContent = error.message || "保存失败";
    } finally {
        submit.disabled = false;
    }
}

function validate(data) {
    let valid = true;
    if (!data.name) { setFieldError("name", "请输入事件名称"); valid = false; }
    if (!data.description) { setFieldError("description", "请输入事件描述"); valid = false; }
    if ([...data.description].length > 50) { setFieldError("description", "事件描述不能超过50个字符"); valid = false; }
    if (!data.initiator) { setFieldError("initiator", "请选择发起人"); valid = false; }
    if (!data.affectedCharacters.length) { setFieldError("affectedCharacters", "请至少选择一位被影响人"); valid = false; }
    return valid;
}

async function refresh() {
    [state.events, state.graph] = await Promise.all([api.getEvents(), api.getGraph()]);
    render();
}

function renderGraphNow() {
    if (stopGraph) stopGraph();
    stopGraph = renderGraph($("#graph"), state.graph);
}

function updateAffectedAvailability() {
    const initiator = $("#event-initiator").value;
    document.querySelectorAll("[name=affectedCharacters]").forEach(input => {
        input.disabled = input.value === initiator;
        if (input.disabled) input.checked = false;
    });
}

function updateDescriptionCount() {
    $("#description-count").textContent = `${[...$("#event-description").value].length} / 50`;
}

function updateAffection() {
    const value = Number($("#event-affection").value);
    const output = $("#affection-value");
    output.textContent = value > 0 ? `+${value}` : value;
    output.className = `score-badge ${tone(value)}`;
}

function characterName(id) {
    return state.characters.find(character => character.id === id)?.name || id;
}
function tone(value) { return value > 0 ? "positive" : value < 0 ? "negative" : "neutral"; }
function clearErrors() {
    document.querySelectorAll(".field-error").forEach(element => element.textContent = "");
    $("#form-error").textContent = "";
}
function setFieldError(field, message) {
    const element = document.querySelector(`[data-error-for="${field}"]`);
    if (element) element.textContent = message;
}
function showToast(message) {
    const toast = $("#toast");
    toast.textContent = message;
    toast.classList.add("visible");
    clearTimeout(showToast.timer);
    showToast.timer = setTimeout(() => toast.classList.remove("visible"), 2600);
}
function escapeHtml(value) {
    const div = document.createElement("div");
    div.textContent = value;
    return div.innerHTML;
}
function debounce(callback, delay) {
    let timer;
    return () => { clearTimeout(timer); timer = setTimeout(callback, delay); };
}
