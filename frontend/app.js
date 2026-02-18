// API URL Configuration
const API_URL = 'http://localhost:8080';
const AUTH_ENDPOINTS = {
  login: `${API_URL}/api/auth/signin`,
  register: `${API_URL}/api/auth/signup`
};
const NOTES_ENDPOINTS = {
  all: `${API_URL}/api/notes`,
  byId: (id) => `${API_URL}/api/notes/${id}`
};
const TODOLISTS_ENDPOINTS = {
  all: `${API_URL}/api/todo-lists`,
  byId: (id) => `${API_URL}/api/todo-lists/${id}`,
  tasks: (listId) => `${API_URL}/api/todo-lists/${listId}/tasks`,
  taskById: (listId, taskId) => `${API_URL}/api/todo-lists/${listId}/tasks/${taskId}`,
  taskStatus: (listId, taskId) => `${API_URL}/api/todo-lists/${listId}/tasks/${taskId}/status`
};

// State Management
const appState = {
  currentUser: null,
  authToken: localStorage.getItem('authToken'),
  notes: [],
  todoLists: [],
  currentNote: null,
  currentTodoList: null
};

// DOM Elements
const elements = {
  // Auth elements
  authContainer: document.getElementById('auth-container'),
  loginForm: document.getElementById('login-form-element'),
  registerForm: document.getElementById('register-form-element'),
  authTabs: document.querySelectorAll('.auth-tab'),
  authForms: document.querySelectorAll('.auth-form'),
  
  // User info
  welcomeMessage: document.getElementById('welcome-message'),
  logoutBtn: document.getElementById('logout-btn'),
  
  // Content container
  contentContainer: document.getElementById('content-container'),
  
  // Notes elements
  notesList: document.getElementById('notes-list'),
  noteView: document.getElementById('note-view'),
  noteId: document.getElementById('note-id'),
  noteTitle: document.getElementById('note-title'),
  noteContent: document.getElementById('note-content'),
  noteTags: document.getElementById('note-tags'),
  newNoteBtn: document.getElementById('new-note-btn'),
  saveNoteBtn: document.getElementById('save-note-btn'),
  deleteNoteBtn: document.getElementById('delete-note-btn'),
  
  // TodoList elements
  todoListsList: document.getElementById('todolists-list'),
  todoListView: document.getElementById('todolist-view'),
  todoListId: document.getElementById('todolist-id'),
  todoListName: document.getElementById('todolist-name'),
  todoListDescription: document.getElementById('todolist-description'),
  newTodoListBtn: document.getElementById('new-todolist-btn'),
  saveTodoListBtn: document.getElementById('save-todolist-btn'),
  deleteTodoListBtn: document.getElementById('delete-todolist-btn'),
  
  // Tasks elements
  tasksList: document.getElementById('tasks-list'),
  newTask: document.getElementById('new-task'),
  addTaskBtn: document.getElementById('add-task-btn'),
  
  // Toast
  toast: document.getElementById('toast')
};

// Initialize the app
function initApp() {
  // Check if user is authenticated
  if (appState.authToken) {
    // Show content and fetch data
    showContent();
    fetchUserData();
    fetchNotes();
    fetchTodoLists();
  } else {
    // Show auth forms
    showAuth();
  }
  
  // Set up event listeners
  setupEventListeners();
}

// Authentication Functions
function showAuth() {
  elements.authContainer.classList.remove('hidden');
  elements.contentContainer.classList.add('hidden');
  elements.logoutBtn.classList.add('hidden');
}

function showContent() {
  elements.authContainer.classList.add('hidden');
  elements.contentContainer.classList.remove('hidden');
  elements.logoutBtn.classList.remove('hidden');
}

async function login(username, password) {
  try {
    const response = await fetch(AUTH_ENDPOINTS.login, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ username, password })
    });
    
    if (!response.ok) {
      throw new Error('Login failed');
    }
    
    const data = await response.json();
    appState.authToken = data.token;
    localStorage.setItem('authToken', data.token);
    
    appState.currentUser = {
      id: data.id,
      username: data.username,
      email: data.email
    };
    
    showContent();
    fetchNotes();
    fetchTodoLists();
    showToast('Login successful!');
  } catch (error) {
    showToast(error.message, true);
  }
}

async function register(username, email, password, firstName, lastName) {
  try {
    const response = await fetch(AUTH_ENDPOINTS.register, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ 
        username, 
        email, 
        password,
        firstName,
        lastName 
      })
    });
    
    if (!response.ok) {
      throw new Error('Registration failed');
    }
    
    const data = await response.json();
    showToast('Registration successful! Please login.');
    
    // Switch to login tab
    switchAuthTab('login');
  } catch (error) {
    showToast(error.message, true);
  }
}

function logout() {
  appState.authToken = null;
  appState.currentUser = null;
  localStorage.removeItem('authToken');
  showAuth();
  showToast('Logged out successfully');
}

// Notes Functions
async function fetchNotes() {
  try {
    const response = await fetch(NOTES_ENDPOINTS.all, {
      headers: {
        'Authorization': `Bearer ${appState.authToken}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch notes');
    }
    
    const data = await response.json();
    appState.notes = data.content || [];
    renderNotesList();
  } catch (error) {
    showToast(error.message, true);
  }
}

function renderNotesList() {
  elements.notesList.innerHTML = '';
  
  appState.notes.forEach(note => {
    const li = document.createElement('li');
    li.textContent = note.title;
    li.dataset.id = note.id;
    
    li.addEventListener('click', () => {
      selectNote(note.id);
    });
    
    elements.notesList.appendChild(li);
  });
}

async function selectNote(id) {
  // Clear any active list items
  document.querySelectorAll('.item-list li').forEach(li => {
    li.classList.remove('active');
  });
  
  try {
    const response = await fetch(NOTES_ENDPOINTS.byId(id), {
      headers: {
        'Authorization': `Bearer ${appState.authToken}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch note');
    }
    
    const note = await response.json();
    appState.currentNote = note;
    
    // Display note in editor
    elements.noteId.value = note.id;
    elements.noteTitle.value = note.title;
    elements.noteContent.value = note.content || '';
    
    const tagNames = note.tags ? note.tags.map(tag => tag.name).join(', ') : '';
    elements.noteTags.value = tagNames;
    
    // Show note view, hide todo list view
    elements.noteView.classList.remove('hidden');
    elements.todoListView.classList.add('hidden');
    
    // Highlight selected note
    document.querySelector(`.item-list li[data-id="${id}"]`).classList.add('active');
  } catch (error) {
    showToast(error.message, true);
  }
}

function createNewNote() {
  appState.currentNote = null;
  elements.noteId.value = '';
  elements.noteTitle.value = '';
  elements.noteContent.value = '';
  elements.noteTags.value = '';
  
  elements.noteView.classList.remove('hidden');
  elements.todoListView.classList.add('hidden');
  
  // Clear any active list items
  document.querySelectorAll('.item-list li').forEach(li => {
    li.classList.remove('active');
  });
}

async function saveNote() {
  const id = elements.noteId.value;
  const title = elements.noteTitle.value;
  const content = elements.noteContent.value;
  const tagsString = elements.noteTags.value;
  
  if (!title) {
    showToast('Note title is required', true);
    return;
  }
  
  const tags = tagsString.split(',').map(tag => tag.trim()).filter(tag => tag);
  const noteData = {
    title,
    content,
    tags
  };
  
  try {
    let url = NOTES_ENDPOINTS.all;
    let method = 'POST';
    
    if (id) {
      url = NOTES_ENDPOINTS.byId(id);
      method = 'PUT';
    }
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${appState.authToken}`
      },
      body: JSON.stringify(noteData)
    });
    
    if (!response.ok) {
      throw new Error('Failed to save note');
    }
    
    showToast('Note saved successfully');
    fetchNotes();
    
    if (!id) {
      // If it's a new note, fetch the newly created note's ID
      const savedNote = await response.json();
      selectNote(savedNote.id);
    }
  } catch (error) {
    showToast(error.message, true);
  }
}

async function deleteNote() {
  const id = elements.noteId.value;
  
  if (!id) {
    showToast('No note selected', true);
    return;
  }
  
  if (!confirm('Are you sure you want to delete this note?')) {
    return;
  }
  
  try {
    const response = await fetch(NOTES_ENDPOINTS.byId(id), {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${appState.authToken}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to delete note');
    }
    
    showToast('Note deleted successfully');
    createNewNote();
    fetchNotes();
  } catch (error) {
    showToast(error.message, true);
  }
}

// TodoList Functions
async function fetchTodoLists() {
  try {
    const response = await fetch(TODOLISTS_ENDPOINTS.all, {
      headers: {
        'Authorization': `Bearer ${appState.authToken}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch todo lists');
    }
    
    const data = await response.json();
    appState.todoLists = data.content || [];
    renderTodoListsList();
  } catch (error) {
    showToast(error.message, true);
  }
}

function renderTodoListsList() {
  elements.todoListsList.innerHTML = '';
  
  appState.todoLists.forEach(list => {
    const li = document.createElement('li');
    li.textContent = list.name;
    li.dataset.id = list.id;
    
    li.addEventListener('click', () => {
      selectTodoList(list.id);
    });
    
    elements.todoListsList.appendChild(li);
  });
}

async function selectTodoList(id) {
  // Clear any active list items
  document.querySelectorAll('.item-list li').forEach(li => {
    li.classList.remove('active');
  });
  
  try {
    const response = await fetch(TODOLISTS_ENDPOINTS.byId(id), {
      headers: {
        'Authorization': `Bearer ${appState.authToken}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch todo list');
    }
    
    const todoList = await response.json();
    appState.currentTodoList = todoList;
    
    // Display todo list
    elements.todoListId.value = todoList.id;
    elements.todoListName.value = todoList.name;
    elements.todoListDescription.value = todoList.description || '';
    
    // Show todo list view, hide note view
    elements.todoListView.classList.remove('hidden');
    elements.noteView.classList.add('hidden');
    
    // Highlight selected todo list
    document.querySelector(`.item-list li[data-id="${id}"]`).classList.add('active');
    
    // Fetch and render tasks
    fetchTasks(id);
  } catch (error) {
    showToast(error.message, true);
  }
}

function createNewTodoList() {
  appState.currentTodoList = null;
  elements.todoListId.value = '';
  elements.todoListName.value = '';
  elements.todoListDescription.value = '';
  elements.tasksList.innerHTML = '';
  
  elements.todoListView.classList.remove('hidden');
  elements.noteView.classList.add('hidden');
  
  // Clear any active list items
  document.querySelectorAll('.item-list li').forEach(li => {
    li.classList.remove('active');
  });
}

async function saveTodoList() {
  const id = elements.todoListId.value;
  const name = elements.todoListName.value;
  const description = elements.todoListDescription.value;
  
  if (!name) {
    showToast('Todo list name is required', true);
    return;
  }
  
  const todoListData = {
    name,
    description
  };
  
  try {
    let url = TODOLISTS_ENDPOINTS.all;
    let method = 'POST';
    
    if (id) {
      url = TODOLISTS_ENDPOINTS.byId(id);
      method = 'PUT';
    }
    
    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${appState.authToken}`
      },
      body: JSON.stringify(todoListData)
    });
    
    if (!response.ok) {
      throw new Error('Failed to save todo list');
    }
    
    showToast('Todo list saved successfully');
    fetchTodoLists();
    
    if (!id) {
      // If it's a new todo list, fetch the newly created list's ID
      const savedTodoList = await response.json();
      selectTodoList(savedTodoList.id);
    }
  } catch (error) {
    showToast(error.message, true);
  }
}

async function deleteTodoList() {
  const id = elements.todoListId.value;
  
  if (!id) {
    showToast('No todo list selected', true);
    return;
  }
  
  if (!confirm('Are you sure you want to delete this todo list?')) {
    return;
  }
  
  try {
    const response = await fetch(TODOLISTS_ENDPOINTS.byId(id), {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${appState.authToken}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to delete todo list');
    }
    
    showToast('Todo list deleted successfully');
    createNewTodoList();
    fetchTodoLists();
  } catch (error) {
    showToast(error.message, true);
  }
}

// Task Functions
async function fetchTasks(todoListId) {
  try {
    const response = await fetch(TODOLISTS_ENDPOINTS.tasks(todoListId), {
      headers: {
        'Authorization': `Bearer ${appState.authToken}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch tasks');
    }
    
    const data = await response.json();
    renderTasks(data.content || []);
  } catch (error) {
    showToast(error.message, true);
  }
}

function renderTasks(tasks) {
  elements.tasksList.innerHTML = '';
  
  tasks.forEach(task => {
    const li = document.createElement('li');
    li.className = 'task-item';
    li.dataset.id = task.id;
    
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.className = 'task-checkbox';
    checkbox.checked = task.status === 'COMPLETED';
    checkbox.addEventListener('change', () => {
      updateTaskStatus(task.id, checkbox.checked ? 'COMPLETED' : 'PENDING');
    });
    
    const taskText = document.createElement('span');
    taskText.className = `task-text ${task.status === 'COMPLETED' ? 'completed' : ''}`;
    taskText.textContent = task.title;
    
    const deleteBtn = document.createElement('button');
    deleteBtn.className = 'btn danger';
    deleteBtn.textContent = 'Delete';
    deleteBtn.addEventListener('click', () => {
      deleteTask(task.id);
    });
    
    const taskActions = document.createElement('div');
    taskActions.className = 'task-actions';
    taskActions.appendChild(deleteBtn);
    
    li.appendChild(checkbox);
    li.appendChild(taskText);
    li.appendChild(taskActions);
    
    elements.tasksList.appendChild(li);
  });
}

async function addTask() {
  const todoListId = elements.todoListId.value;
  const taskTitle = elements.newTask.value.trim();
  
  if (!todoListId) {
    showToast('No todo list selected', true);
    return;
  }
  
  if (!taskTitle) {
    showToast('Task title is required', true);
    return;
  }
  
  const taskData = {
    title: taskTitle,
    status: 'PENDING',
    priority: 'MEDIUM'
  };
  
  try {
    const response = await fetch(TODOLISTS_ENDPOINTS.tasks(todoListId), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${appState.authToken}`
      },
      body: JSON.stringify(taskData)
    });
    
    if (!response.ok) {
      throw new Error('Failed to add task');
    }
    
    elements.newTask.value = '';
    fetchTasks(todoListId);
  } catch (error) {
    showToast(error.message, true);
  }
}

async function updateTaskStatus(taskId, status) {
  const todoListId = elements.todoListId.value;
  
  try {
    const response = await fetch(TODOLISTS_ENDPOINTS.taskStatus(todoListId, taskId), {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${appState.authToken}`
      },
      body: JSON.stringify({ status })
    });
    
    if (!response.ok) {
      throw new Error('Failed to update task status');
    }
    
    // Update UI to reflect the change
    const taskElement = document.querySelector(`.task-item[data-id="${taskId}"] .task-text`);
    if (status === 'COMPLETED') {
      taskElement.classList.add('completed');
    } else {
      taskElement.classList.remove('completed');
    }
  } catch (error) {
    showToast(error.message, true);
    // Revert checkbox state
    const checkbox = document.querySelector(`.task-item[data-id="${taskId}"] .task-checkbox`);
    checkbox.checked = !checkbox.checked;
  }
}

async function deleteTask(taskId) {
  const todoListId = elements.todoListId.value;
  
  if (!confirm('Are you sure you want to delete this task?')) {
    return;
  }
  
  try {
    const response = await fetch(TODOLISTS_ENDPOINTS.taskById(todoListId, taskId), {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${appState.authToken}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to delete task');
    }
    
    // Remove the task element from the UI
    document.querySelector(`.task-item[data-id="${taskId}"]`).remove();
  } catch (error) {
    showToast(error.message, true);
  }
}

// User data
async function fetchUserData() {
  try {
    const response = await fetch(`${API_URL}/api/users/me`, {
      headers: {
        'Authorization': `Bearer ${appState.authToken}`
      }
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch user data');
    }
    
    const userData = await response.json();
    appState.currentUser = userData;
    
    // Update welcome message
    elements.welcomeMessage.textContent = `Welcome, ${userData.firstName || userData.username}`;
  } catch (error) {
    showToast(error.message, true);
  }
}

// Utility Functions
function setupEventListeners() {
  // Auth tabs
  elements.authTabs.forEach(tab => {
    tab.addEventListener('click', () => {
      switchAuthTab(tab.dataset.tab);
    });
  });
  
  // Auth forms
  elements.loginForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    login(username, password);
  });
  
  elements.registerForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const username = document.getElementById('register-username').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    const firstName = document.getElementById('register-first-name').value;
    const lastName = document.getElementById('register-last-name').value;
    register(username, email, password, firstName, lastName);
  });
  
  // Logout button
  elements.logoutBtn.addEventListener('click', logout);
  
  // Notes
  elements.newNoteBtn.addEventListener('click', createNewNote);
  elements.saveNoteBtn.addEventListener('click', saveNote);
  elements.deleteNoteBtn.addEventListener('click', deleteNote);
  
  // TodoLists
  elements.newTodoListBtn.addEventListener('click', createNewTodoList);
  elements.saveTodoListBtn.addEventListener('click', saveTodoList);
  elements.deleteTodoListBtn.addEventListener('click', deleteTodoList);
  
  // Tasks
  elements.addTaskBtn.addEventListener('click', addTask);
  elements.newTask.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
      addTask();
    }
  });
}

function switchAuthTab(tab) {
  // Remove active class from all tabs and forms
  elements.authTabs.forEach(t => t.classList.remove('active'));
  elements.authForms.forEach(f => f.classList.remove('active'));
  
  // Add active class to selected tab and form
  document.querySelector(`.auth-tab[data-tab="${tab}"]`).classList.add('active');
  document.getElementById(`${tab}-form`).classList.add('active');
}

function showToast(message, isError = false) {
  const toast = elements.toast;
  toast.textContent = message;
  toast.className = isError ? 'toast error show' : 'toast show';
  
  setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}

// Initialize the app when the DOM is loaded
document.addEventListener('DOMContentLoaded', initApp);
