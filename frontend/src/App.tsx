import React, { useEffect, useState } from 'react';
import './App.css';
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';
import { motion, AnimatePresence } from 'framer-motion';
import Player from 'lottie-react';
import avatarAnimation from './lottie/avatar.json';
import Dashboard from './components/Dashboard';
import NotificationCenter from './components/NotificationCenter';

export type Task = {
  id: number;
  title: string;
  description: string;
  dueDate: string;
  status: string;
  priority: string;
  assignedTo?: any;
};

export type User = {
  id: number;
  username: string;
  email: string;
  avatarUrl: string;
};

const columns = [
  { 
    key: 'To Do', 
    label: 'To Do', 
    icon: '📋',
    className: 'todo',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  { 
    key: 'In Progress', 
    label: 'In Progress', 
    icon: '⚡',
    className: 'inprogress',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  { 
    key: 'Done', 
    label: 'Done', 
    icon: '✅',
    className: 'done',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
];

type TasksByStatus = { [status: string]: Task[] };

function groupTasks(tasks: Task[]): TasksByStatus {
  return columns.reduce((acc, col) => {
    acc[col.key] = tasks.filter(t => t.status === col.key);
    return acc;
  }, {} as TasksByStatus);
}

function App() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    title: '',
    description: '',
    dueDate: '',
    status: 'To Do',
    priority: 'Medium',
    assignedToId: '',
  });
  const [loading, setLoading] = useState(false);
  const [editTask, setEditTask] = useState<Task | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [showHistoryId, setShowHistoryId] = useState<number | null>(null);
  const [history, setHistory] = useState<any[]>([]);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [filterStatus, setFilterStatus] = useState('');
  const [filterPriority, setFilterPriority] = useState('');
  const [sortBy, setSortBy] = useState('');
  const [viewMode, setViewMode] = useState<'kanban' | 'dashboard'>('kanban');

  useEffect(() => {
    // Load tasks and users
    Promise.all([
      fetch('/api/tasks').then(res => res.json()),
      fetch('/api/users').then(res => res.json())
    ]).then(([tasksData, usersData]) => {
      setTasks(tasksData || []);
      setUsers(usersData || []);
    }).catch(() => {
      setTasks([]);
      setUsers([]);
    });
  }, []);

  const filteredTasks = tasks.filter(t =>
    (!filterStatus || t.status === filterStatus) &&
    (!filterPriority || t.priority === filterPriority)
  );

  const sortedTasks = [...filteredTasks].sort((a, b) => {
    if (sortBy === 'dueDate') {
      return (a.dueDate || '').localeCompare(b.dueDate || '');
    } else if (sortBy === 'priority') {
      const order = { High: 1, Medium: 2, Low: 3 };
      return (order[a.priority as 'High'|'Medium'|'Low'] || 4) - (order[b.priority as 'High'|'Medium'|'Low'] || 4);
    }
    return 0;
  });

  const tasksByStatus = groupTasks(sortedTasks);

  // Statistics
  const stats = {
    total: tasks.length,
    todo: tasks.filter(t => t.status === 'To Do').length,
    inProgress: tasks.filter(t => t.status === 'In Progress').length,
    done: tasks.filter(t => t.status === 'Done').length,
    highPriority: tasks.filter(t => t.priority === 'High').length,
    overdue: tasks.filter(t => t.dueDate && new Date(t.dueDate) < new Date()).length,
  };

  const onDragEnd = async (result: DropResult) => {
    if (!result.destination) return;
    const { source, destination, draggableId } = result;
    if (source.droppableId === destination.droppableId && source.index === destination.index) return;
    
    const taskId = Number(draggableId);
    const task = tasks.find(t => t.id === taskId);
    if (!task) return;

    // Update task status locally
    const updatedTask = { ...task, status: destination.droppableId };
    setTasks(prev => prev.map(t => t.id === taskId ? updatedTask : t));

    // Update on server
    try {
      await fetch(`/api/tasks/${taskId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updatedTask),
      });
      showToast(`Task moved to ${destination.droppableId}!`);
    } catch (error) {
      // Revert on error
      setTasks(prev => prev.map(t => t.id === taskId ? task : t));
      showToast('Failed to update task status');
    }
  };

  const handleFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleEditClick = (task: Task) => {
    setEditTask(task);
    setForm({
      title: task.title,
      description: task.description,
      dueDate: task.dueDate,
      status: task.status,
      priority: task.priority,
      assignedToId: task.assignedTo?.id?.toString() || '',
    });
    setShowForm(true);
  };

  const handleDelete = async (taskId: number) => {
    try {
      await fetch(`/api/tasks/${taskId}`, { method: 'DELETE' });
      setTasks(prev => prev.filter(t => t.id !== taskId));
      showToast('Task deleted successfully!');
    } catch {
      showToast('Failed to delete task');
    }
  };

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 3000);
  };

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    
    const formData = {
      ...form,
      assignedTo: form.assignedToId ? { id: parseInt(form.assignedToId) } : null
    };

    try {
      if (editTask) {
        // Edit task
        const res = await fetch(`/api/tasks/${editTask.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ ...formData, id: editTask.id }),
        });
        if (res.ok) {
          const updated = await res.json();
          setTasks(prev => prev.map(t => t.id === updated.id ? updated : t));
          showToast('Task updated successfully!');
        }
      } else {
        // Add task
        const res = await fetch('/api/tasks', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(formData),
        });
        if (res.ok) {
          const newTask = await res.json();
          setTasks(prev => [...prev, newTask]);
          showToast('Task created successfully!');
        }
      }
      
      setShowForm(false);
      setEditTask(null);
      setForm({ title: '', description: '', dueDate: '', status: 'To Do', priority: 'Medium', assignedToId: '' });
    } catch {
      showToast('Failed to save task');
    }
    setLoading(false);
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditTask(null);
    setForm({ title: '', description: '', dueDate: '', status: 'To Do', priority: 'Medium', assignedToId: '' });
  };

  const handleShowHistory = async (task: Task) => {
    setShowHistoryId(task.id);
    setLoadingHistory(true);
    try {
      const res = await fetch(`/api/tasks/${task.id}`);
      if (res.ok) {
        const data = await res.json();
        const log = JSON.parse(data.changeLog || '[]');
        setHistory(log);
      }
    } catch {
      setHistory([]);
    }
    setLoadingHistory(false);
  };

  const handleCloseHistory = () => {
    setShowHistoryId(null);
    setHistory([]);
  };

  const clearFilters = () => {
    setFilterStatus('');
    setFilterPriority('');
    setSortBy('');
  };

  const getPriorityIcon = (priority: string) => {
    switch (priority) {
      case 'High': return '🔥';
      case 'Medium': return '⚡';
      case 'Low': return '🌿';
      default: return '📋';
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric'
    });
  };

  const isOverdue = (dateString: string) => {
    if (!dateString) return false;
    return new Date(dateString) < new Date();
  };

  return (
    <div className="App">
      <div className="app-background"></div>
      
      {/* Header */}
      <header className="app-header">
        <div className="header-content">
          <div className="app-title">
            <div className="title-icon">📊</div>
            <h1>Team Task Manager</h1>
          </div>
        </div>
      </header>

      {/* User Panel */}
      <motion.div 
        className="user-panel"
        initial={{ opacity: 0, x: 100 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="user-panel-content">
          <div className="avatar-container">
            <Player 
              animationData={avatarAnimation} 
              loop 
              style={{ width: 30, height: 30 }}
            />
          </div>
          <div className="user-info">
            <div className="user-name">John Doe</div>
            <div className="user-email">john@example.com</div>
          </div>
        </div>
      </motion.div>

      {/* Notification Center */}
      <NotificationCenter tasks={tasks} />

      {/* Statistics Section */}
      <motion.section 
        className="stats-section"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.2 }}
      >
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-header">
              <div className="stat-title">Total Tasks</div>
              <div className="stat-icon" style={{background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'}}>
                📊
              </div>
            </div>
            <div className="stat-value">{stats.total}</div>
          </div>
          
          <div className="stat-card">
            <div className="stat-header">
              <div className="stat-title">In Progress</div>
              <div className="stat-icon" style={{background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'}}>
                ⚡
              </div>
            </div>
            <div className="stat-value">{stats.inProgress}</div>
          </div>
          
          <div className="stat-card">
            <div className="stat-header">
              <div className="stat-title">Completed</div>
              <div className="stat-icon" style={{background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'}}>
                ✅
              </div>
            </div>
            <div className="stat-value">{stats.done}</div>
          </div>
          
          <div className="stat-card">
            <div className="stat-header">
              <div className="stat-title">High Priority</div>
              <div className="stat-icon" style={{background: 'linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%)'}}>
                🔥
              </div>
            </div>
            <div className="stat-value">{stats.highPriority}</div>
          </div>
        </div>
      </motion.section>

      {/* Controls Section */}
      <motion.section 
        className="controls-section"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5, delay: 0.3 }}
      >
        <div className="controls-grid">
          <div className="control-group">
            <label className="control-label">Filter by Status</label>
            <select 
              className="modern-select" 
              value={filterStatus} 
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="To Do">To Do</option>
              <option value="In Progress">In Progress</option>
              <option value="Done">Done</option>
            </select>
          </div>
          
          <div className="control-group">
            <label className="control-label">Filter by Priority</label>
            <select 
              className="modern-select" 
              value={filterPriority} 
              onChange={(e) => setFilterPriority(e.target.value)}
            >
              <option value="">All Priorities</option>
              <option value="High">High</option>
              <option value="Medium">Medium</option>
              <option value="Low">Low</option>
            </select>
          </div>
          
          <div className="control-group">
            <label className="control-label">Sort by</label>
            <select 
              className="modern-select" 
              value={sortBy} 
              onChange={(e) => setSortBy(e.target.value)}
            >
              <option value="">No Sorting</option>
              <option value="dueDate">Due Date</option>
              <option value="priority">Priority</option>
            </select>
          </div>
          
          <div className="control-group">
            <label className="control-label">Actions</label>
            <div style={{display: 'flex', gap: '10px'}}>
              <button 
                className="modern-button primary" 
                onClick={() => setShowForm(true)}
              >
                ➕ Add Task
              </button>
              <button 
                className="modern-button secondary" 
                onClick={clearFilters}
              >
                🔄 Clear Filters
              </button>
            </div>
          </div>
          
          <div className="control-group">
            <label className="control-label">View Mode</label>
            <div style={{display: 'flex', gap: '10px'}}>
              <button 
                className={`modern-button ${viewMode === 'kanban' ? 'primary' : 'secondary'}`}
                onClick={() => setViewMode('kanban')}
              >
                📋 Kanban
              </button>
              <button 
                className={`modern-button ${viewMode === 'dashboard' ? 'primary' : 'secondary'}`}
                onClick={() => setViewMode('dashboard')}
              >
                📊 Dashboard
              </button>
            </div>
          </div>
        </div>
      </motion.section>

      {/* Task Form */}
      <AnimatePresence>
        {showForm && (
          <motion.form 
            className="task-form"
            onSubmit={handleFormSubmit}
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.9 }}
            transition={{ duration: 0.3 }}
          >
            <h2 style={{ marginBottom: '20px', textAlign: 'center' }}>
              {editTask ? '✏️ Edit Task' : '➕ Create New Task'}
            </h2>
            
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">Title</label>
                <input
                  className="form-input"
                  type="text"
                  name="title"
                  value={form.title}
                  onChange={handleFormChange}
                  required
                  placeholder="Enter task title..."
                />
              </div>
              
              <div className="form-group">
                <label className="form-label">Status</label>
                <select
                  className="form-select"
                  name="status"
                  value={form.status}
                  onChange={handleFormChange}
                >
                  <option value="To Do">📋 To Do</option>
                  <option value="In Progress">⚡ In Progress</option>
                  <option value="Done">✅ Done</option>
                </select>
              </div>
              
              <div className="form-group">
                <label className="form-label">Priority</label>
                <select
                  className="form-select"
                  name="priority"
                  value={form.priority}
                  onChange={handleFormChange}
                >
                  <option value="Low">🌿 Low</option>
                  <option value="Medium">⚡ Medium</option>
                  <option value="High">🔥 High</option>
                </select>
              </div>
              
              <div className="form-group">
                <label className="form-label">Due Date</label>
                <input
                  className="form-input"
                  type="date"
                  name="dueDate"
                  value={form.dueDate}
                  onChange={handleFormChange}
                />
              </div>
              
              <div className="form-group">
                <label className="form-label">Assign To</label>
                <select
                  className="form-select"
                  name="assignedToId"
                  value={form.assignedToId}
                  onChange={handleFormChange}
                >
                  <option value="">Unassigned</option>
                  {users.map(user => (
                    <option key={user.id} value={user.id}>
                      👤 {user.username}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            
            <div className="form-group">
              <label className="form-label">Description</label>
              <textarea
                className="form-textarea"
                name="description"
                value={form.description}
                onChange={handleFormChange}
                placeholder="Enter task description..."
                rows={3}
              />
            </div>
            
            <div className="form-actions">
              <button 
                type="button" 
                className="modern-button secondary" 
                onClick={handleCancel}
                disabled={loading}
              >
                Cancel
              </button>
              <button 
                type="submit" 
                className="modern-button success"
                disabled={loading}
              >
                {loading ? <div className="loading"></div> : (editTask ? 'Update Task' : 'Create Task')}
              </button>
            </div>
          </motion.form>
        )}
      </AnimatePresence>

      {/* Main Content Area */}
      {viewMode === 'dashboard' ? (
        <Dashboard tasks={tasks} users={users} />
      ) : (
        <div className="kanban-container">
          <DragDropContext onDragEnd={onDragEnd}>
            <motion.div 
              className="kanban-board"
              initial={{ opacity: 0, y: 50 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.4 }}
            >
              {columns.map((column) => (
                <div key={column.key} className={`kanban-column ${column.className}`}>
                  <div className="column-header">
                    <div className="column-title">
                      <div 
                        className="column-icon"
                        style={{ background: column.gradient }}
                      >
                        {column.icon}
                      </div>
                      {column.label}
                    </div>
                    <div className="column-count">
                      {tasksByStatus[column.key]?.length || 0}
                    </div>
                  </div>
                  
                  <Droppable droppableId={column.key}>
                    {(provided, snapshot) => (
                      <div
                        ref={provided.innerRef}
                        {...provided.droppableProps}
                        style={{
                          minHeight: 200,
                          opacity: snapshot.isDraggingOver ? 0.8 : 1,
                          transition: 'opacity 0.2s'
                        }}
                      >
                        <AnimatePresence>
                          {(tasksByStatus[column.key] || []).map((task, index) => (
                            <Draggable key={task.id} draggableId={task.id.toString()} index={index}>
                              {(provided, snapshot) => (
                                <div
                                  ref={provided.innerRef}
                                  {...provided.draggableProps}
                                  {...provided.dragHandleProps}
                                  style={{
                                    ...provided.draggableProps.style,
                                    transform: snapshot.isDragging 
                                      ? `${provided.draggableProps.style?.transform} rotate(5deg)` 
                                      : provided.draggableProps.style?.transform
                                  }}
                                >
                                  <motion.div
                                    className="kanban-card"
                                    initial={{ opacity: 0, y: 20 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, x: -100 }}
                                    whileHover={{ y: -2 }}
                                    layout
                                  >
                                    <div className="card-header">
                                      <h3 className="card-title">{task.title}</h3>
                                      <div className={`card-priority priority-${task.priority.toLowerCase()}`}>
                                        {getPriorityIcon(task.priority)} {task.priority}
                                      </div>
                                    </div>
                                    
                                    <p className="card-description">{task.description}</p>
                                    
                                    {task.assignedTo && (
                                      <div style={{ 
                                        display: 'flex', 
                                        alignItems: 'center', 
                                        gap: '8px', 
                                        marginBottom: '10px',
                                        fontSize: '0.9rem',
                                        color: '#666'
                                      }}>
                                        👤 {task.assignedTo.username}
                                      </div>
                                    )}
                                    
                                    <div className="card-footer">
                                      <div className="card-due-date">
                                        {task.dueDate && (
                                          <>
                                            📅 
                                            <span style={{ 
                                              color: isOverdue(task.dueDate) ? '#ff6b6b' : 'inherit',
                                              fontWeight: isOverdue(task.dueDate) ? 'bold' : 'normal'
                                            }}>
                                              {formatDate(task.dueDate)}
                                              {isOverdue(task.dueDate) && ' (Overdue)'}
                                            </span>
                                          </>
                                        )}
                                      </div>
                                      
                                      <div className="card-actions">
                                        <button
                                          className="card-button"
                                          onClick={() => handleShowHistory(task)}
                                        >
                                          📜 History
                                        </button>
                                        <button
                                          className="card-button"
                                          onClick={() => handleEditClick(task)}
                                        >
                                          ✏️ Edit
                                        </button>
                                        <button
                                          className="card-button danger"
                                          onClick={() => handleDelete(task.id)}
                                        >
                                          🗑️ Delete
                                        </button>
                                      </div>
                                    </div>
                                  </motion.div>
                                </div>
                              )}
                            </Draggable>
                          ))}
                        </AnimatePresence>
                        {provided.placeholder}
                      </div>
                    )}
                  </Droppable>
                </div>
              ))}
            </motion.div>
          </DragDropContext>
        </div>
      )}

      {/* Toast Notification */}
      <AnimatePresence>
        {toast && (
          <motion.div
            className="toast"
            initial={{ opacity: 0, x: 100 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: 100 }}
            transition={{ duration: 0.3 }}
          >
            {toast}
          </motion.div>
        )}
      </AnimatePresence>

      {/* History Modal */}
      <AnimatePresence>
        {showHistoryId && (
          <motion.div
            className="modal-overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={handleCloseHistory}
          >
            <motion.div
              className="modal-content"
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.8, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <h2 style={{ marginBottom: '20px' }}>📜 Task History</h2>
              
              {loadingHistory ? (
                <div style={{ textAlign: 'center', padding: '20px' }}>
                  <div className="loading"></div>
                </div>
              ) : history.length > 0 ? (
                <div>
                  {history.map((entry, index) => (
                    <div key={index} className="timeline-entry">
                      <div>{entry.action}</div>
                      <div className="timeline-date">{entry.timestamp}</div>
                    </div>
                  ))}
                </div>
              ) : (
                <p>No history available for this task.</p>
              )}
              
              <div style={{ textAlign: 'right', marginTop: '20px' }}>
                <button 
                  className="modern-button secondary" 
                  onClick={handleCloseHistory}
                >
                  Close
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

export default App;
