export function getNodesFromLocalStorage() {
    return JSON.parse(localStorage.getItem("nodes") || "[]");
}

export function saveNodesToLocalStorage(nodes) {
    localStorage.setItem("nodes", JSON.stringify(nodes));
}
