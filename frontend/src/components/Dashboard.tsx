import React from 'react';
import { motion } from 'framer-motion';
import { Task, User } from '../App';
import { exportTasksToCSV, exportUsersToCSV, exportProjectSummary } from '../utils/csvExport';
import { OPTIMIZED_STYLES, GPU_OPTIMIZED } from '../animations/performance';

interface DashboardProps {
  tasks: Task[];
  users: User[];
}

const Dashboard: React.FC<DashboardProps> = ({ tasks, users }) => {
  // Advanced statistics
  const stats = {
    total: tasks.length,
    todo: tasks.filter(t => t.status === 'To Do').length,
    inProgress: tasks.filter(t => t.status === 'In Progress').length,
    done: tasks.filter(t => t.status === 'Done').length,
    highPriority: tasks.filter(t => t.priority === 'High').length,
    mediumPriority: tasks.filter(t => t.priority === 'Medium').length,
    lowPriority: tasks.filter(t => t.priority === 'Low').length,
    overdue: tasks.filter(t => t.dueDate && new Date(t.dueDate) < new Date()).length,
    assigned: tasks.filter(t => t.assignedTo).length,
    unassigned: tasks.filter(t => !t.assignedTo).length,
  };

  // User statistics
  const userStats = users.map(user => ({
    ...user,
    taskCount: tasks.filter(t => t.assignedTo?.id === user.id).length,
    completedTasks: tasks.filter(t => t.assignedTo?.id === user.id && t.status === 'Done').length,
    inProgressTasks: tasks.filter(t => t.assignedTo?.id === user.id && t.status === 'In Progress').length,
  }));

  // Progress calculation
  const completionRate = stats.total > 0 ? Math.round((stats.done / stats.total) * 100) : 0;
  const productivityScore = stats.total > 0 ? Math.round(((stats.done + stats.inProgress * 0.5) / stats.total) * 100) : 0;

  // Chart data for priority distribution
  const priorityData = [
    { label: 'High', count: stats.highPriority, color: 'var(--danger-500)', percentage: stats.total > 0 ? Math.round((stats.highPriority / stats.total) * 100) : 0 },
    { label: 'Medium', count: stats.mediumPriority, color: 'var(--warning-500)', percentage: stats.total > 0 ? Math.round((stats.mediumPriority / stats.total) * 100) : 0 },
    { label: 'Low', count: stats.lowPriority, color: 'var(--success-500)', percentage: stats.total > 0 ? Math.round((stats.lowPriority / stats.total) * 100) : 0 },
  ];

  const statusData = [
    { label: 'To Do', count: stats.todo, color: 'var(--primary-500)', percentage: stats.total > 0 ? Math.round((stats.todo / stats.total) * 100) : 0 },
    { label: 'In Progress', count: stats.inProgress, color: 'var(--warning-500)', percentage: stats.total > 0 ? Math.round((stats.inProgress / stats.total) * 100) : 0 },
    { label: 'Done', count: stats.done, color: 'var(--success-500)', percentage: stats.total > 0 ? Math.round((stats.done / stats.total) * 100) : 0 },
  ];

  const ProgressBar: React.FC<{ percentage: number; color: string; delay?: number }> = ({ percentage, color, delay = 0 }) => (
    <div style={OPTIMIZED_STYLES.progressContainer}>
      <motion.div
        style={{
          ...OPTIMIZED_STYLES.progressFill,
          background: color,
        }}
        initial={{ transform: GPU_OPTIMIZED.progressTransform(0) }}
        animate={{ transform: GPU_OPTIMIZED.progressTransform(percentage) }}
        transition={{ 
          duration: 1.5, 
          delay,
          ease: "easeOut"
        }}
      />
    </div>
  );

  const CircularProgress: React.FC<{ percentage: number; size: number; strokeWidth: number; color: string }> = ({ 
    percentage, 
    size, 
    strokeWidth, 
    color 
  }) => {
    const radius = (size - strokeWidth) / 2;
    const circumference = radius * 2 * Math.PI;
    const strokeDasharray = `${circumference} ${circumference}`;
    const strokeDashoffset = circumference - (percentage / 100) * circumference;

    return (
      <div style={{ position: 'relative', display: 'inline-block' }}>
        <svg
          height={size}
          width={size}
          style={{ transform: 'rotate(-90deg)' }}
        >
          <circle
            stroke="var(--border-glass)"
            fill="transparent"
            strokeWidth={strokeWidth}
            r={radius}
            cx={size / 2}
            cy={size / 2}
          />
          <motion.circle
            stroke={color}
            fill="transparent"
            strokeWidth={strokeWidth}
            strokeDasharray={strokeDasharray}
            r={radius}
            cx={size / 2}
            cy={size / 2}
            initial={{ strokeDashoffset: circumference }}
            animate={{ strokeDashoffset }}
            transition={{ duration: 2, ease: "easeInOut" }}
            strokeLinecap="round"
          />
        </svg>
        <div style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          textAlign: 'center'
        }}>
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 1, duration: 0.5 }}
            style={{ fontSize: '1.5rem', fontWeight: 'bold', color: 'var(--text-primary)' }}
          >
            {percentage}%
          </motion.div>
        </div>
      </div>
    );
  };

  return (
    <motion.div
      className="dashboard"
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      style={{
        background: 'var(--bg-card)',
        backdropFilter: 'blur(25px)',
        border: 'var(--border-width) solid var(--border-glass)',
        borderRadius: 'var(--radius-2xl)',
        padding: 'var(--spacing-2xl)',
        margin: '0 var(--spacing-lg) var(--spacing-2xl) var(--spacing-lg)',
        boxShadow: 'var(--shadow-xl)',
        position: 'relative',
        zIndex: 40,
      }}
    >
      <motion.h2
        style={{ 
          textAlign: 'center', 
          marginBottom: 'var(--spacing-2xl)',
          background: 'var(--bg-primary)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
          backgroundClip: 'text',
          fontSize: 'clamp(1.5rem, 4vw, 2rem)',
          fontWeight: '900',
          letterSpacing: '-0.02em'
        }}
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        📊 Project Dashboard
      </motion.h2>

      {/* Overall Progress Section */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', 
        gap: 'var(--spacing-2xl)',
        marginBottom: 'var(--spacing-3xl)'
      }}>
        <motion.div
          style={{
            background: 'var(--bg-surface)',
            border: 'var(--border-width) solid var(--border-glass)',
            borderRadius: 'var(--radius-xl)',
            padding: 'var(--spacing-xl)',
            textAlign: 'center',
            boxShadow: 'var(--shadow-lg)',
          }}
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.3 }}
          whileHover={{ scale: 1.02, y: -5 }}
        >
          <h3 style={{ marginBottom: 'var(--spacing-lg)', color: 'var(--text-primary)', fontSize: '1.1rem', fontWeight: '700' }}>📈 Completion Rate</h3>
          <CircularProgress 
            percentage={completionRate} 
            size={120} 
            strokeWidth={8} 
            color="var(--success-500)" 
          />
        </motion.div>

        <motion.div
          style={{
            background: 'var(--bg-surface)',
            border: 'var(--border-width) solid var(--border-glass)',
            borderRadius: 'var(--radius-xl)',
            padding: 'var(--spacing-xl)',
            textAlign: 'center',
            boxShadow: 'var(--shadow-lg)',
          }}
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.4 }}
          whileHover={{ scale: 1.02, y: -5 }}
        >
          <h3 style={{ marginBottom: 'var(--spacing-lg)', color: 'var(--text-primary)', fontSize: '1.1rem', fontWeight: '700' }}>⚡ Productivity Score</h3>
          <CircularProgress 
            percentage={productivityScore} 
            size={120} 
            strokeWidth={8} 
            color="var(--primary-500)" 
          />
        </motion.div>
      </div>

      {/* Priority Distribution */}
      <motion.div
        style={{
          background: 'var(--bg-surface)',
          border: 'var(--border-width) solid var(--border-glass)',
          borderRadius: 'var(--radius-xl)',
          padding: 'var(--spacing-xl)',
          marginBottom: 'var(--spacing-2xl)',
          boxShadow: 'var(--shadow-lg)',
        }}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.5 }}
      >
        <h3 style={{ marginBottom: 'var(--spacing-lg)', color: 'var(--text-primary)', fontSize: '1.1rem', fontWeight: '700' }}>🔥 Priority Distribution</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-lg)' }}>
          {priorityData.map((item, index) => (
            <motion.div
              key={item.label}
              style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-lg)' }}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.6 + index * 0.1 }}
            >
              <div style={{ 
                minWidth: '80px', 
                fontSize: '0.9rem', 
                fontWeight: '700',
                color: 'var(--text-primary)'
              }}>
                {item.label}
              </div>
              <div style={{ flex: 1 }}>
                <ProgressBar percentage={item.percentage} color={item.color} delay={0.8 + index * 0.1} />
              </div>
              <div style={{ 
                minWidth: '80px', 
                textAlign: 'right',
                fontSize: '0.9rem',
                fontWeight: '700',
                color: 'var(--text-primary)'
              }}>
                {item.count} ({item.percentage}%)
              </div>
            </motion.div>
          ))}
        </div>
      </motion.div>

      {/* Status Distribution */}
      <motion.div
        style={{
          background: 'var(--bg-surface)',
          border: 'var(--border-width) solid var(--border-glass)',
          borderRadius: 'var(--radius-xl)',
          padding: 'var(--spacing-xl)',
          marginBottom: 'var(--spacing-2xl)',
          boxShadow: 'var(--shadow-lg)',
        }}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.7 }}
      >
        <h3 style={{ marginBottom: 'var(--spacing-lg)', color: 'var(--text-primary)', fontSize: '1.1rem', fontWeight: '700' }}>📋 Status Distribution</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-lg)' }}>
          {statusData.map((item, index) => (
            <motion.div
              key={item.label}
              style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-lg)' }}
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.8 + index * 0.1 }}
            >
              <div style={{ 
                minWidth: '100px', 
                fontSize: '0.9rem', 
                fontWeight: '700',
                color: 'var(--text-primary)'
              }}>
                {item.label}
              </div>
              <div style={{ flex: 1 }}>
                <ProgressBar percentage={item.percentage} color={item.color} delay={1 + index * 0.1} />
              </div>
              <div style={{ 
                minWidth: '80px', 
                textAlign: 'right',
                fontSize: '0.9rem',
                fontWeight: '700',
                color: 'var(--text-primary)'
              }}>
                {item.count} ({item.percentage}%)
              </div>
            </motion.div>
          ))}
        </div>
      </motion.div>

      {/* Team Performance */}
      <motion.div
        style={{
          background: 'var(--bg-surface)',
          border: 'var(--border-width) solid var(--border-glass)',
          borderRadius: 'var(--radius-xl)',
          padding: 'var(--spacing-xl)',
          marginBottom: 'var(--spacing-2xl)',
          boxShadow: 'var(--shadow-lg)',
        }}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.9 }}
      >
        <h3 style={{ marginBottom: 'var(--spacing-lg)', color: 'var(--text-primary)', fontSize: '1.1rem', fontWeight: '700' }}>👥 Team Performance</h3>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', 
          gap: 'var(--spacing-xl)' 
        }}>
          {userStats.map((user, index) => (
            <motion.div
              key={user.id}
              style={{
                background: 'var(--bg-card)',
                border: 'var(--border-width) solid var(--border-glass)',
                borderRadius: 'var(--radius-lg)',
                padding: 'var(--spacing-lg)',
                boxShadow: 'var(--shadow-md)',
              }}
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 1 + index * 0.1 }}
              whileHover={{ scale: 1.02, y: -3 }}
            >
              <div style={{ 
                display: 'flex', 
                alignItems: 'center', 
                gap: 'var(--spacing-md)',
                marginBottom: 'var(--spacing-lg)'
              }}>
                <div style={{
                  width: '48px',
                  height: '48px',
                  borderRadius: 'var(--radius-full)',
                  background: 'var(--bg-primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--text-on-primary)',
                  fontWeight: 'bold',
                  fontSize: '1.2rem',
                  boxShadow: 'var(--shadow-md)'
                }}>
                  {user.username.charAt(0)}
                </div>
                <div>
                  <div style={{ fontWeight: '700', color: 'var(--text-primary)', fontSize: '1.1rem' }}>
                    {user.username}
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                    {user.email}
                  </div>
                </div>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-sm)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', fontWeight: '600' }}>Total Tasks:</span>
                  <span style={{ fontWeight: '700', color: 'var(--text-primary)' }}>{user.taskCount}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', fontWeight: '600' }}>Completed:</span>
                  <span style={{ fontWeight: '700', color: 'var(--success-500)' }}>{user.completedTasks}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', fontWeight: '600' }}>In Progress:</span>
                  <span style={{ fontWeight: '700', color: 'var(--warning-500)' }}>{user.inProgressTasks}</span>
                </div>
                <div style={{ marginTop: 'var(--spacing-md)' }}>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: 'var(--spacing-xs)' }}>
                    Completion Rate
                  </div>
                  <ProgressBar 
                    percentage={user.taskCount > 0 ? Math.round((user.completedTasks / user.taskCount) * 100) : 0} 
                    color="var(--success-500)" 
                    delay={1.2 + index * 0.1} 
                  />
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      </motion.div>

      {/* Quick Stats */}
      <motion.div
        style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', 
          gap: 'var(--spacing-lg)',
          marginTop: 'var(--spacing-2xl)'
        }}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 1.2 }}
      >
        {[
          { label: 'Overdue', value: stats.overdue, icon: '⚠️', color: 'var(--danger-500)' },
          { label: 'Assigned', value: stats.assigned, icon: '👤', color: 'var(--primary-500)' },
          { label: 'Unassigned', value: stats.unassigned, icon: '❓', color: 'var(--warning-500)' },
        ].map((stat, index) => (
          <motion.div
            key={stat.label}
            style={{
              background: 'var(--bg-surface)',
              border: `var(--border-width) solid ${stat.color}30`,
              borderRadius: 'var(--radius-lg)',
              padding: 'var(--spacing-lg)',
              textAlign: 'center',
              boxShadow: 'var(--shadow-md)',
            }}
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 1.3 + index * 0.1 }}
            whileHover={{ scale: 1.05, y: -2 }}
          >
            <div style={{ fontSize: '2rem', marginBottom: 'var(--spacing-sm)' }}>{stat.icon}</div>
            <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: stat.color }}>
              {stat.value}
            </div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', fontWeight: '600' }}>
              {stat.label}
            </div>
          </motion.div>
        ))}
      </motion.div>

      {/* Export Section */}
      <motion.div
        style={{
          background: 'var(--bg-surface)',
          border: 'var(--border-width) solid var(--border-glass)',
          borderRadius: 'var(--radius-xl)',
          padding: 'var(--spacing-xl)',
          marginTop: 'var(--spacing-2xl)',
          boxShadow: 'var(--shadow-lg)',
        }}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 1.4 }}
      >
        <h3 style={{ marginBottom: 'var(--spacing-lg)', color: 'var(--text-primary)', fontSize: '1.1rem', fontWeight: '700' }}>📄 Export Data</h3>
        <div style={{ 
          display: 'grid', 
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', 
          gap: 'var(--spacing-md)' 
        }}>
          <motion.button
            style={{
              background: 'var(--bg-primary)',
              color: 'var(--text-on-primary)',
              border: 'none',
              borderRadius: 'var(--radius-lg)',
              padding: 'var(--spacing-lg) var(--spacing-xl)',
              fontSize: '0.9rem',
              fontWeight: '700',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 'var(--spacing-sm)',
              boxShadow: 'var(--shadow-md)',
              minHeight: '48px',
            }}
            onClick={() => exportTasksToCSV(tasks)}
            whileHover={{ scale: 1.02, y: -2 }}
            whileTap={{ scale: 0.98 }}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 1.5 }}
          >
            📋 Export Tasks
          </motion.button>

          <motion.button
            style={{
              background: 'var(--bg-secondary)',
              color: 'var(--text-on-primary)',
              border: 'none',
              borderRadius: 'var(--radius-lg)',
              padding: 'var(--spacing-lg) var(--spacing-xl)',
              fontSize: '0.9rem',
              fontWeight: '700',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 'var(--spacing-sm)',
              boxShadow: 'var(--shadow-md)',
              minHeight: '48px',
            }}
            onClick={() => exportUsersToCSV(users, tasks)}
            whileHover={{ scale: 1.02, y: -2 }}
            whileTap={{ scale: 0.98 }}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 1.6 }}
          >
            👥 Export Team Report
          </motion.button>

          <motion.button
            style={{
              background: 'var(--bg-success)',
              color: 'var(--text-on-primary)',
              border: 'none',
              borderRadius: 'var(--radius-lg)',
              padding: 'var(--spacing-lg) var(--spacing-xl)',
              fontSize: '0.9rem',
              fontWeight: '700',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 'var(--spacing-sm)',
              boxShadow: 'var(--shadow-md)',
              minHeight: '48px',
            }}
            onClick={() => exportProjectSummary(tasks, users)}
            whileHover={{ scale: 1.02, y: -2 }}
            whileTap={{ scale: 0.98 }}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 1.7 }}
          >
            📊 Export Summary
          </motion.button>
        </div>
      </motion.div>
    </motion.div>
  );
};

export default Dashboard; 