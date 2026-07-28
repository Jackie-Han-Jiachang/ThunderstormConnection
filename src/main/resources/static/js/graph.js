const SVG_NS = "http://www.w3.org/2000/svg";

function element(name, attributes = {}) {
    const node = document.createElementNS(SVG_NS, name);
    Object.entries(attributes).forEach(([key, value]) => node.setAttribute(key, value));
    return node;
}

function tone(score) {
    return score > 0 ? "positive" : score < 0 ? "negative" : "neutral";
}

export function renderGraph(container, graph) {
    const svg = container.querySelector("svg");
    const empty = container.querySelector(".empty-state");
    svg.replaceChildren();
    if (!graph.nodes.length) {
        empty.hidden = false;
        return;
    }
    empty.hidden = true;

    const width = Math.max(container.clientWidth, 420);
    const height = Math.max(container.clientHeight, 350);
    svg.setAttribute("viewBox", `0 0 ${width} ${height}`);
    const nodes = graph.nodes.map((node, index) => ({
        ...node,
        x: width / 2 + Math.cos(index * Math.PI * 2 / graph.nodes.length) * Math.min(width, height) * .28,
        y: height / 2 + Math.sin(index * Math.PI * 2 / graph.nodes.length) * Math.min(width, height) * .28,
        vx: 0, vy: 0, fixed: false
    }));
    const byId = new Map(nodes.map(node => [node.id, node]));
    const links = graph.links.map(link => ({...link, source: byId.get(link.source), target: byId.get(link.target)}));

    const linkGroup = element("g");
    const scoreGroup = element("g");
    const nodeGroup = element("g");
    svg.append(linkGroup, scoreGroup, nodeGroup);
    links.forEach(link => {
        link.line = element("line", {class: `graph-link ${tone(link.score)}`, "stroke-width": Math.min(7, 2 + Math.abs(link.score) * .14)});
        link.label = element("text", {class: "graph-score"});
        link.label.textContent = link.score > 0 ? `+${link.score}` : `${link.score}`;
        linkGroup.append(link.line);
        scoreGroup.append(link.label);
    });

    nodes.forEach(node => {
        const group = element("g", {class: "graph-node", tabindex: "0", role: "button", "aria-label": node.name});
        group.append(element("circle", {r: 29}));
        const label = element("text", {dy: 5});
        label.textContent = node.name;
        group.append(label);
        nodeGroup.append(group);
        node.group = group;
        addDrag(group, node, svg);
    });

    let frame;
    let iterations = 0;
    function tick() {
        const strength = Math.max(.01, .09 * (1 - iterations / 420));
        for (let i = 0; i < nodes.length; i++) {
            for (let j = i + 1; j < nodes.length; j++) {
                const a = nodes[i], b = nodes[j];
                let dx = b.x - a.x, dy = b.y - a.y;
                const distanceSquared = Math.max(dx * dx + dy * dy, 100);
                const force = 950 / distanceSquared;
                const distance = Math.sqrt(distanceSquared);
                dx /= distance; dy /= distance;
                a.vx -= dx * force; a.vy -= dy * force;
                b.vx += dx * force; b.vy += dy * force;
            }
        }
        links.forEach(link => {
            const dx = link.target.x - link.source.x, dy = link.target.y - link.source.y;
            const distance = Math.max(Math.hypot(dx, dy), 1);
            const force = (distance - link.distance) * .009;
            const fx = dx / distance * force, fy = dy / distance * force;
            link.source.vx += fx; link.source.vy += fy;
            link.target.vx -= fx; link.target.vy -= fy;
        });
        nodes.forEach(node => {
            if (!node.fixed) {
                node.vx += (width / 2 - node.x) * strength * .008;
                node.vy += (height / 2 - node.y) * strength * .008;
                node.vx *= .86; node.vy *= .86;
                node.x = Math.max(36, Math.min(width - 36, node.x + node.vx));
                node.y = Math.max(36, Math.min(height - 36, node.y + node.vy));
            }
            node.group.setAttribute("transform", `translate(${node.x} ${node.y})`);
        });
        links.forEach(link => {
            link.line.setAttribute("x1", link.source.x); link.line.setAttribute("y1", link.source.y);
            link.line.setAttribute("x2", link.target.x); link.line.setAttribute("y2", link.target.y);
            link.label.setAttribute("x", (link.source.x + link.target.x) / 2);
            link.label.setAttribute("y", (link.source.y + link.target.y) / 2 - 6);
        });
        iterations++;
        if (iterations < 420 || nodes.some(node => node.fixed)) frame = requestAnimationFrame(tick);
    }
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
}

function addDrag(group, node, svg) {
    const point = event => {
        const rect = svg.getBoundingClientRect();
        const view = svg.viewBox.baseVal;
        return {
            x: (event.clientX - rect.left) * view.width / rect.width,
            y: (event.clientY - rect.top) * view.height / rect.height
        };
    };
    group.addEventListener("pointerdown", event => {
        node.fixed = true;
        group.setPointerCapture(event.pointerId);
        Object.assign(node, point(event));
    });
    group.addEventListener("pointermove", event => {
        if (group.hasPointerCapture(event.pointerId)) Object.assign(node, point(event));
    });
    group.addEventListener("pointerup", event => {
        node.fixed = false;
        group.releasePointerCapture(event.pointerId);
    });
}
