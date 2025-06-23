import React from 'react';
import { motion } from 'framer-motion';
import { Draggable } from '@hello-pangea/dnd';

export interface Task {
  id: number;
  title: string;
  description: string;
  dueDate: string;
  status: string;
  priority: string;
  assignedTo?: {
    id: number;
    username: string;
    email: string;
    avatarUrl: string;
  };
}

interface TaskCardProps {
  task: Task;
  index: number;
  onEdit: (task: Task) => void;
  onDelete: (taskId: number) => void;
  onShowHistory: (task: Task) => void;
}

const TaskCard: React.FC<TaskCardProps> = ({ task, index, onEdit, onDelete, onShowHistory }) => {
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

  const getPriorityGradient = (priority: string) => {
    switch (priority) {
      case 'High': return 'linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%)';
      case 'Medium': return 'linear-gradient(135deg, #feca57 0%, #ff9ff3 100%)';
      case 'Low': return 'linear-gradient(135deg, #48cae4 0%, #0077b6 100%)';
      default: return 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
    }
  };

  return (
    <Draggable draggableId={task.id.toString()} index={index}>
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
            exit={{ opacity: 0, x: -100, scale: 0.8 }}
            whileHover={{ 
              y: -5, 
              boxShadow: "0 25px 50px -12px rgba(0, 0, 0, 0.25)",
              transition: { duration: 0.2 }
            }}
            layout
            transition={{
              layout: { duration: 0.3 },
              opacity: { duration: 0.3 },
              y: { duration: 0.3 }
            }}
          >
            {/* Priority stripe */}
            <motion.div
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                height: '4px',
                background: getPriorityGradient(task.priority),
                borderRadius: '16px 16px 0 0'
              }}
              initial={{ scaleX: 0 }}
              animate={{ scaleX: 1 }}
              transition={{ delay: 0.1, duration: 0.5 }}
            />

            <div className="card-header">
              <motion.h3 
                className="card-title"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.2 }}
              >
                {task.title}
              </motion.h3>
              <motion.div 
                className={`card-priority priority-${task.priority.toLowerCase()}`}
                initial={{ opacity: 0, scale: 0.5 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.3, type: "spring" }}
                whileHover={{ scale: 1.1 }}
              >
                {getPriorityIcon(task.priority)} {task.priority}
              </motion.div>
            </div>
            
            <motion.p 
              className="card-description"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.4 }}
            >
              {task.description}
            </motion.p>
            
            {task.assignedTo && (
              <motion.div 
                style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '8px', 
                  marginBottom: '10px',
                  fontSize: '0.9rem',
                  color: '#666',
                  padding: '8px 12px',
                  background: 'rgba(102, 126, 234, 0.1)',
                  borderRadius: '12px',
                  border: '1px solid rgba(102, 126, 234, 0.2)'
                }}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.5 }}
                whileHover={{ 
                  background: 'rgba(102, 126, 234, 0.15)',
                  transition: { duration: 0.2 }
                }}
              >
                <motion.div
                  initial={{ rotate: 0 }}
                  animate={{ rotate: 360 }}
                  transition={{ delay: 0.6, duration: 0.5 }}
                >
                  👤
                </motion.div>
                <span>{task.assignedTo.username}</span>
              </motion.div>
            )}
            
            <div className="card-footer">
              <motion.div 
                className="card-due-date"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.6 }}
              >
                {task.dueDate && (
                  <>
                    <motion.span
                      initial={{ rotate: 0 }}
                      animate={{ rotate: 360 }}
                      transition={{ delay: 0.7, duration: 0.5 }}
                    >
                      📅
                    </motion.span>
                    <span style={{ 
                      color: isOverdue(task.dueDate) ? '#ff6b6b' : 'inherit',
                      fontWeight: isOverdue(task.dueDate) ? 'bold' : 'normal'
                    }}>
                      {formatDate(task.dueDate)}
                      {isOverdue(task.dueDate) && (
                        <motion.span
                          initial={{ opacity: 0, x: -10 }}
                          animate={{ opacity: 1, x: 0 }}
                          transition={{ delay: 0.8 }}
                          style={{ 
                            marginLeft: '4px',
                            color: '#ff6b6b',
                            fontSize: '0.8rem',
                            fontWeight: 'bold'
                          }}
                        >
                          (Overdue)
                        </motion.span>
                      )}
                    </span>
                  </>
                )}
              </motion.div>
              
              <div className="card-actions">
                <motion.button
                  className="card-button"
                  onClick={() => onShowHistory(task)}
                  whileHover={{ scale: 1.05, y: -1 }}
                  whileTap={{ scale: 0.95 }}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.7 }}
                >
                  📜 History
                </motion.button>
                <motion.button
                  className="card-button"
                  onClick={() => onEdit(task)}
                  whileHover={{ scale: 1.05, y: -1 }}
                  whileTap={{ scale: 0.95 }}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.8 }}
                >
                  ✏️ Edit
                </motion.button>
                <motion.button
                  className="card-button danger"
                  onClick={() => onDelete(task.id)}
                  whileHover={{ 
                    scale: 1.05, 
                    y: -1,
                    boxShadow: "0 8px 25px rgba(255, 107, 107, 0.3)"
                  }}
                  whileTap={{ scale: 0.95 }}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.9 }}
                >
                  🗑️ Delete
                </motion.button>
              </div>
            </div>

            {/* Hover effect overlay */}
            <motion.div
              style={{
                position: 'absolute',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                background: 'linear-gradient(45deg, transparent 30%, rgba(255,255,255,0.1) 50%, transparent 70%)',
                borderRadius: '16px',
                pointerEvents: 'none',
                opacity: 0
              }}
              whileHover={{ 
                opacity: 1,
                transition: { duration: 0.3 }
              }}
            />
          </motion.div>
        </div>
      )}
    </Draggable>
  );
};

export default TaskCard; 