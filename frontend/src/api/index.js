import axios from 'axios';

const apiClient = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json',
    },
});

// JWT 토큰을 헤더에 추가하는 인터셉터
apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

// Project APIs
export const getProjects = () => apiClient.get('/projects');
export const getProjectById = (projectId) => apiClient.get(`/projects/${projectId}`);
export const createProject = (data) => apiClient.post('/projects', data);
export const updateProject = (projectId, data) => apiClient.put(`/projects/${projectId}`, data);
export const deleteProject = (projectId) => apiClient.delete(`/projects/${projectId}`);

// Board APIs
export const getBoardByProject = (projectId, boardId) => apiClient.get(`/boards/projects/${projectId}/boards/${boardId}`);
export const createBoard = (projectId, data) => apiClient.post(`/boards/projects/${projectId}/boards`, data);
export const updateBoard = (boardId, data) => apiClient.put(`/boards/${boardId}`, data);
export const deleteBoard = (boardId) => apiClient.delete(`/boards/${boardId}`);

// Kanban Column APIs
export const getColumnsByBoard = (boardId) => apiClient.get(`/kanban-columns/boards/${boardId}`);
export const createColumn = (boardId, data) => apiClient.post(`/kanban-columns/boards/${boardId}`, data);
export const updateColumn = (columnId, data) => apiClient.put(`/kanban-columns/${columnId}`, data);
export const updateColumnPositions = (boardId, columnIds) => apiClient.patch(`/kanban-columns/boards/${boardId}/positions`, columnIds);
export const deleteColumn = (columnId) => apiClient.delete(`/kanban-columns/${columnId}`);

// Card APIs
export const getCardsByProject = (projectId) => apiClient.get(`/cards/project/${projectId}`);
export const createCard = (columnId, data) => apiClient.post(`/cards/columns/${columnId}`, data);
export const updateCard = (cardId, data) => apiClient.put(`/cards/${cardId}`, data);
export const deleteCard = (cardId) => apiClient.delete(`/cards/${cardId}`);
export const shiftCard = (cardId, data) => apiClient.patch(`/cards/${cardId}/shift`, data);

// Tag APIs
export const getTagsByProject = (projectId) => apiClient.get(`/tags/projects/${projectId}`);
export const createTag = (projectId, data) => apiClient.post(`/tags/projects/${projectId}`, data);
export const updateTag = (tagId, data) => apiClient.put(`/tags/${tagId}`, data);
export const deleteTag = (tagId) => apiClient.delete(`/tags/${tagId}`);

// CardTag APIs
export const addTagToCard = (cardId, tagId) => apiClient.post(`/cards/${cardId}/tags`, { tagId });
export const removeTagFromCard = (cardId, tagId) => apiClient.delete(`/cards/${cardId}/tags/${tagId}`);

// User APIs
export const login = (data) => apiClient.post('/users/login', data);
export const signup = (data) => apiClient.post('/users/signup', data);

