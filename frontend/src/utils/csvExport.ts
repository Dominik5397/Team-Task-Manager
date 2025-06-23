import { Task, User } from '../App';

export const exportTasksToCSV = (tasks: Task[]) => {
  const headers = [
    'ID',
    'Title',
    'Description',
    'Status',
    'Priority',
    'Due Date',
    'Assigned To',
    'Created Date'
  ];

  const csvContent = [
    headers.join(','),
    ...tasks.map(task => [
      task.id,
      `"${task.title.replace(/"/g, '""')}"`,
      `"${task.description.replace(/"/g, '""')}"`,
      task.status,
      task.priority,
      task.dueDate || '',
      task.assignedTo ? `"${task.assignedTo.username}"` : 'Unassigned',
      new Date().toISOString().split('T')[0] // Current date as created date
    ].join(','))
  ].join('\n');

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  
  link.setAttribute('href', url);
  link.setAttribute('download', `tasks_export_${new Date().toISOString().split('T')[0]}.csv`);
  link.style.visibility = 'hidden';
  
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

export const exportUsersToCSV = (users: User[], tasks: Task[]) => {
  const headers = [
    'ID',
    'Username',
    'Email',
    'Total Tasks',
    'Completed Tasks',
    'In Progress Tasks',
    'To Do Tasks',
    'Completion Rate (%)'
  ];

  const csvContent = [
    headers.join(','),
    ...users.map(user => {
      const userTasks = tasks.filter(t => t.assignedTo?.id === user.id);
      const completed = userTasks.filter(t => t.status === 'Done').length;
      const inProgress = userTasks.filter(t => t.status === 'In Progress').length;
      const todo = userTasks.filter(t => t.status === 'To Do').length;
      const completionRate = userTasks.length > 0 ? Math.round((completed / userTasks.length) * 100) : 0;

      return [
        user.id,
        `"${user.username.replace(/"/g, '""')}"`,
        `"${user.email.replace(/"/g, '""')}"`,
        userTasks.length,
        completed,
        inProgress,
        todo,
        completionRate
      ].join(',');
    })
  ].join('\n');

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  
  link.setAttribute('href', url);
  link.setAttribute('download', `users_report_${new Date().toISOString().split('T')[0]}.csv`);
  link.style.visibility = 'hidden';
  
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

export const exportProjectSummary = (tasks: Task[], users: User[]) => {
  const stats = {
    totalTasks: tasks.length,
    completedTasks: tasks.filter(t => t.status === 'Done').length,
    inProgressTasks: tasks.filter(t => t.status === 'In Progress').length,
    todoTasks: tasks.filter(t => t.status === 'To Do').length,
    highPriorityTasks: tasks.filter(t => t.priority === 'High').length,
    mediumPriorityTasks: tasks.filter(t => t.priority === 'Medium').length,
    lowPriorityTasks: tasks.filter(t => t.priority === 'Low').length,
    overdueTasks: tasks.filter(t => t.dueDate && new Date(t.dueDate) < new Date()).length,
    assignedTasks: tasks.filter(t => t.assignedTo).length,
    unassignedTasks: tasks.filter(t => !t.assignedTo).length,
    totalUsers: users.length,
  };

  const completionRate = tasks.length > 0 ? Math.round((stats.completedTasks / tasks.length) * 100) : 0;

  const summaryData = [
    ['Project Summary Report', ''],
    ['Generated on', new Date().toLocaleDateString()],
    ['', ''],
    ['Task Statistics', ''],
    ['Total Tasks', stats.totalTasks],
    ['Completed Tasks', stats.completedTasks],
    ['In Progress Tasks', stats.inProgressTasks],
    ['To Do Tasks', stats.todoTasks],
    ['Completion Rate (%)', completionRate],
    ['', ''],
    ['Priority Distribution', ''],
    ['High Priority', stats.highPriorityTasks],
    ['Medium Priority', stats.mediumPriorityTasks],
    ['Low Priority', stats.lowPriorityTasks],
    ['', ''],
    ['Assignment Status', ''],
    ['Assigned Tasks', stats.assignedTasks],
    ['Unassigned Tasks', stats.unassignedTasks],
    ['Overdue Tasks', stats.overdueTasks],
    ['', ''],
    ['Team Information', ''],
    ['Total Team Members', stats.totalUsers],
  ];

  const csvContent = summaryData.map(row => row.join(',')).join('\n');

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  
  link.setAttribute('href', url);
  link.setAttribute('download', `project_summary_${new Date().toISOString().split('T')[0]}.csv`);
  link.style.visibility = 'hidden';
  
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}; 