import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Task } from '../App';

interface Notification {
  id: string;
  type: 'info' | 'success' | 'warning' | 'error';
  title: string;
  message: string;
  timestamp: Date;
  icon: string;
}

interface NotificationCenterProps {
  tasks: Task[];
}

const NotificationCenter: React.FC<NotificationCenterProps> = ({ tasks }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [showCenter, setShowCenter] = useState(false);

  useEffect(() => {
    // Check for overdue tasks
    const overdueTasks = tasks.filter(task => 
      task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'Done'
    );

    // Check for tasks due today
    const today = new Date().toISOString().split('T')[0];
    const tasksDueToday = tasks.filter(task => 
      task.dueDate === today && task.status !== 'Done'
    );

    // Check for tasks due tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const tomorrowStr = tomorrow.toISOString().split('T')[0];
    const tasksDueTomorrow = tasks.filter(task => 
      task.dueDate === tomorrowStr && task.status !== 'Done'
    );

    // Generate notifications
    const newNotifications: Notification[] = [];

    if (overdueTasks.length > 0) {
      newNotifications.push({
        id: 'overdue-' + Date.now(),
        type: 'error',
        title: 'Overdue Tasks',
        message: `You have ${overdueTasks.length} overdue task(s) that need attention.`,
        timestamp: new Date(),
        icon: '⚠️'
      });
    }

    if (tasksDueToday.length > 0) {
      newNotifications.push({
        id: 'today-' + Date.now(),
        type: 'warning',
        title: 'Tasks Due Today',
        message: `${tasksDueToday.length} task(s) are due today.`,
        timestamp: new Date(),
        icon: '📅'
      });
    }

    if (tasksDueTomorrow.length > 0) {
      newNotifications.push({
        id: 'tomorrow-' + Date.now(),
        type: 'info',
        title: 'Tasks Due Tomorrow',
        message: `${tasksDueTomorrow.length} task(s) are due tomorrow.`,
        timestamp: new Date(),
        icon: '📋'
      });
    }

    // Check completion rate for motivational messages
    const completedTasks = tasks.filter(task => task.status === 'Done').length;
    const totalTasks = tasks.length;
    const completionRate = totalTasks > 0 ? (completedTasks / totalTasks) * 100 : 0;

    if (completionRate >= 80 && totalTasks > 0) {
      newNotifications.push({
        id: 'achievement-' + Date.now(),
        type: 'success',
        title: 'Great Progress!',
        message: `You've completed ${Math.round(completionRate)}% of your tasks. Keep it up!`,
        timestamp: new Date(),
        icon: '🎉'
      });
    }

    setNotifications(newNotifications);
  }, [tasks]);

  const getNotificationStyle = (type: string) => {
    const baseStyle = {
      background: 'var(--bg-card)',
      borderRadius: 'var(--radius-md)',
      padding: '15px',
      marginBottom: '10px',
      boxShadow: 'var(--shadow-md)',
      border: '1px solid',
      position: 'relative' as const,
      overflow: 'hidden' as const,
    };

    switch (type) {
      case 'error':
        return { ...baseStyle, borderColor: '#ff6b6b', borderLeftWidth: '4px', borderLeftColor: '#ff6b6b' };
      case 'warning':
        return { ...baseStyle, borderColor: '#feca57', borderLeftWidth: '4px', borderLeftColor: '#feca57' };
      case 'info':
        return { ...baseStyle, borderColor: '#667eea', borderLeftWidth: '4px', borderLeftColor: '#667eea' };
      case 'success':
        return { ...baseStyle, borderColor: '#4facfe', borderLeftWidth: '4px', borderLeftColor: '#4facfe' };
      default:
        return baseStyle;
    }
  };

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false 
    });
  };

  const unreadCount = notifications.length;

  return (
    <>
      {/* Notification Bell */}
      <motion.div
        style={{
          position: 'fixed',
          top: '20px',
          right: '140px',
          background: 'var(--bg-glass)',
          backdropFilter: 'blur(20px)',
          border: '1px solid var(--border-glass)',
          borderRadius: '50%',
          width: '60px',
          height: '60px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          boxShadow: 'var(--shadow-lg)',
          zIndex: 1002,
        }}
        onClick={() => setShowCenter(!showCenter)}
        whileHover={{ scale: 1.05, y: -2 }}
        whileTap={{ scale: 0.95 }}
        initial={{ opacity: 0, scale: 0 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3, delay: 0.5 }}
      >
        <motion.div
          style={{ position: 'relative' }}
          animate={{ rotate: unreadCount > 0 ? [0, -10, 10, -10, 0] : 0 }}
          transition={{ 
            duration: 0.5, 
            repeat: unreadCount > 0 ? Infinity : 0, 
            repeatDelay: 3 
          }}
        >
          🔔
        </motion.div>
        {unreadCount > 0 && (
          <motion.div
            style={{
              position: 'absolute',
              top: '5px',
              right: '5px',
              background: '#ff6b6b',
              color: 'white',
              borderRadius: '50%',
              width: '20px',
              height: '20px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '0.8rem',
              fontWeight: 'bold',
            }}
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: "spring", stiffness: 300 }}
          >
            {unreadCount > 9 ? '9+' : unreadCount}
          </motion.div>
        )}
      </motion.div>

      {/* Notification Center Panel */}
      <AnimatePresence>
        {showCenter && (
          <motion.div
            style={{
              position: 'fixed',
              top: '90px',
              right: '20px',
              width: '350px',
              maxHeight: '70vh',
              background: 'var(--bg-glass)',
              backdropFilter: 'blur(20px)',
              border: '1px solid var(--border-glass)',
              borderRadius: 'var(--radius-xl)',
              boxShadow: 'var(--shadow-xl)',
              zIndex: 2000,
              overflow: 'hidden',
            }}
            initial={{ opacity: 0, y: -20, scale: 0.9 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -20, scale: 0.9 }}
            transition={{ duration: 0.3 }}
          >
            {/* Header */}
            <div style={{
              padding: '20px 25px',
              borderBottom: '1px solid var(--border-glass)',
              background: 'var(--bg-card)',
            }}>
              <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center' 
              }}>
                <h3 style={{ 
                  margin: 0, 
                  color: 'var(--text-primary)',
                  fontWeight: '700'
                }}>
                  🔔 Notifications
                </h3>
                <motion.button
                  style={{
                    background: 'none',
                    border: 'none',
                    fontSize: '1.2rem',
                    cursor: 'pointer',
                    padding: '5px',
                    borderRadius: '50%',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                  onClick={() => setShowCenter(false)}
                  whileHover={{ background: 'rgba(0,0,0,0.1)' }}
                  whileTap={{ scale: 0.9 }}
                >
                  ✕
                </motion.button>
              </div>
              {unreadCount > 0 && (
                <p style={{ 
                  margin: '5px 0 0 0', 
                  fontSize: '0.9rem', 
                  color: 'var(--text-secondary)' 
                }}>
                  {unreadCount} unread notification{unreadCount !== 1 ? 's' : ''}
                </p>
              )}
            </div>

            {/* Notifications List */}
            <div style={{
              maxHeight: '400px',
              overflowY: 'auto',
              padding: '15px',
            }}>
              {notifications.length === 0 ? (
                <motion.div
                  style={{
                    textAlign: 'center',
                    padding: '40px 20px',
                    color: 'var(--text-secondary)',
                  }}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: 0.2 }}
                >
                  <div style={{ fontSize: '3rem', marginBottom: '10px' }}>🎉</div>
                  <p>All caught up!</p>
                  <p style={{ fontSize: '0.9rem' }}>No new notifications.</p>
                </motion.div>
              ) : (
                <AnimatePresence>
                  {notifications.map((notification, index) => (
                    <motion.div
                      key={notification.id}
                      style={getNotificationStyle(notification.type)}
                      initial={{ opacity: 0, x: 50 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: -50 }}
                      transition={{ delay: index * 0.1 }}
                      whileHover={{ y: -2, boxShadow: 'var(--shadow-lg)' }}
                    >
                      {/* Animated background for success notifications */}
                      {notification.type === 'success' && (
                        <motion.div
                          style={{
                            position: 'absolute',
                            top: 0,
                            left: 0,
                            right: 0,
                            bottom: 0,
                            background: 'linear-gradient(45deg, transparent 30%, rgba(255,255,255,0.1) 50%, transparent 70%)',
                            borderRadius: 'var(--radius-md)',
                          }}
                          animate={{ x: ['-100%', '100%'] }}
                          transition={{ 
                            duration: 2, 
                            repeat: Infinity, 
                            repeatDelay: 1 
                          }}
                        />
                      )}

                      <div style={{ 
                        display: 'flex', 
                        alignItems: 'flex-start', 
                        gap: '12px',
                        position: 'relative',
                        zIndex: 1,
                      }}>
                        <motion.div
                          style={{ fontSize: '1.5rem' }}
                          animate={{ 
                            scale: notification.type === 'error' ? [1, 1.1, 1] : 1,
                            rotate: notification.type === 'warning' ? [0, -5, 5, 0] : 0
                          }}
                          transition={{ 
                            duration: 0.5, 
                            repeat: notification.type === 'error' ? Infinity : 0, 
                            repeatDelay: 1 
                          }}
                        >
                          {notification.icon}
                        </motion.div>
                        <div style={{ flex: 1 }}>
                          <h4 style={{ 
                            margin: '0 0 5px 0', 
                            fontSize: '0.95rem',
                            fontWeight: '600',
                            color: 'var(--text-primary)'
                          }}>
                            {notification.title}
                          </h4>
                          <p style={{ 
                            margin: '0 0 8px 0', 
                            fontSize: '0.85rem',
                            color: 'var(--text-secondary)',
                            lineHeight: '1.4'
                          }}>
                            {notification.message}
                          </p>
                          <div style={{ 
                            fontSize: '0.75rem', 
                            color: 'var(--text-secondary)',
                            opacity: 0.7
                          }}>
                            {formatTime(notification.timestamp)}
                          </div>
                        </div>
                      </div>
                    </motion.div>
                  ))}
                </AnimatePresence>
              )}
            </div>

            {/* Footer */}
            {notifications.length > 0 && (
              <motion.div
                style={{
                  padding: '15px 25px',
                  borderTop: '1px solid var(--border-glass)',
                  background: 'var(--bg-card)',
                  textAlign: 'center',
                }}
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ delay: 0.5 }}
              >
                <motion.button
                  style={{
                    background: 'var(--bg-primary)',
                    color: 'white',
                    border: 'none',
                    borderRadius: 'var(--radius-md)',
                    padding: '8px 16px',
                    fontSize: '0.85rem',
                    fontWeight: '600',
                    cursor: 'pointer',
                  }}
                  onClick={() => setNotifications([])}
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  Clear All
                </motion.button>
              </motion.div>
            )}
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
};

export default NotificationCenter; 