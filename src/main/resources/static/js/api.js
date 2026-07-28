async function request(path, options = {}) {
    const response = await fetch(path, {
        headers: options.body ? {"Content-Type": "application/json"} : {},
        ...options
    });
    if (!response.ok) {
        const error = await response.json().catch(() => ({message: "请求失败，请稍后重试"}));
        throw error;
    }
    return response.status === 204 ? null : response.json();
}

export const api = {
    getEvents: () => request("/api/events"),
    getCharacters: () => request("/api/characters"),
    getGraph: () => request("/api/graph"),
    createEvent: data => request("/api/events", {method: "POST", body: JSON.stringify(data)}),
    updateEvent: (id, data) => request(`/api/events/${id}`, {method: "PUT", body: JSON.stringify(data)}),
    deleteEvent: id => request(`/api/events/${id}`, {method: "DELETE"}),
    activateEvent: id => request(`/api/events/${id}/activate`, {method: "POST"}),
    revertEvent: id => request(`/api/events/${id}/revert`, {method: "POST"})
};
