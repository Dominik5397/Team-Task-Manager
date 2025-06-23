import React from 'react';
import { motion } from 'framer-motion';

interface StatsCardProps {
  title: string;
  value: number;
  icon: string;
  gradient: string;
  delay?: number;
}

const StatsCard: React.FC<StatsCardProps> = ({ title, value, icon, gradient, delay = 0 }) => {
  return (
    <motion.div
      className="stat-card"
      initial={{ opacity: 0, y: 30, scale: 0.9 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      transition={{ 
        duration: 0.5, 
        delay,
        type: "spring",
        stiffness: 100
      }}
      whileHover={{ 
        y: -8, 
        scale: 1.02,
        transition: { duration: 0.2 }
      }}
    >
      <div className="stat-header">
        <div className="stat-title">{title}</div>
        <motion.div 
          className="stat-icon" 
          style={{ background: gradient }}
          whileHover={{ rotate: 10 }}
          transition={{ duration: 0.2 }}
        >
          {icon}
        </motion.div>
      </div>
      <motion.div 
        className="stat-value"
        initial={{ scale: 0 }}
        animate={{ scale: 1 }}
        transition={{ 
          delay: delay + 0.3,
          type: "spring",
          stiffness: 200
        }}
      >
        {value}
      </motion.div>
    </motion.div>
  );
};

export default StatsCard; 