import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { 
  useOptimizedAnimation, 
  DEBUG_PERFORMANCE, 
  validateAnimationProps,
  GPU_OPTIMIZED,
  PERFORMANCE_VARIANTS,
  OPTIMIZED_STYLES
} from '../animations/performance';

/**
 * Demo component showcasing performance-optimized animations
 * This component validates our 60fps optimization strategy
 */
export const PerformanceDemo: React.FC = () => {
  const [percentage, setPercentage] = useState(0);
  const [showBadExample, setShowBadExample] = useState(false);
  
  const { config, startAnimation, endAnimation, getMetrics } = useOptimizedAnimation('performanceDemo');

  useEffect(() => {
    // Animate progress bar from 0 to 100%
    const timer = setInterval(() => {
      setPercentage(prev => prev < 100 ? prev + 1 : 0);
    }, 50);

    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    // Monitor performance in development
    if (process.env.NODE_ENV === 'development') {
      startAnimation();
      
      // Validate animation props
      const goodProps = { transform: 'scale(1.1)', opacity: 0.8 };
      const badProps = { width: '200px', margin: '10px' };
      
      DEBUG_PERFORMANCE.logAnimationPerformance('goodAnimation', goodProps);
      DEBUG_PERFORMANCE.logAnimationPerformance('badAnimation', badProps);
      
      return () => {
        endAnimation();
        const metrics = getMetrics();
        if (metrics) {
          console.log('Performance Demo Metrics:', metrics);
        }
      };
    }
  }, [startAnimation, endAnimation, getMetrics]);

  const handleToggleBadExample = () => {
    setShowBadExample(!showBadExample);
    
    // Validate the bad animation props
    const result = validateAnimationProps({ 
      width: '300px', 
      height: '200px', 
      margin: '20px' 
    });
    
    if (!result.valid) {
      console.warn('Performance Issues in Bad Example:', result.warnings);
    }
  };

  return (
    <div style={{ padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      <h2>🚀 Animation Performance Demo</h2>
      
      {/* Good Example: GPU-Optimized Progress Bar */}
      <div style={{ marginBottom: '30px' }}>
        <h3>✅ Good: GPU-Optimized Progress Bar (scaleX)</h3>
        <p>Uses <code>transform: scaleX()</code> - GPU accelerated, 60fps</p>
        
        <div style={OPTIMIZED_STYLES.progressContainer}>
          <motion.div
            style={{
              ...OPTIMIZED_STYLES.progressFill,
              background: 'linear-gradient(90deg, #4facfe 0%, #00f2fe 100%)',
            }}
            initial={{ transform: GPU_OPTIMIZED.progressTransform(0) }}
            animate={{ transform: GPU_OPTIMIZED.progressTransform(percentage) }}
            transition={{ duration: 0.1, ease: "linear" }}
          />
        </div>
        <p>Progress: {percentage}%</p>
      </div>

      {/* Bad Example Toggle */}
      <div style={{ marginBottom: '30px' }}>
        <button onClick={handleToggleBadExample}>
          {showBadExample ? 'Hide' : 'Show'} Bad Example (Layout Thrashing)
        </button>
        
        {showBadExample && (
          <div style={{ marginTop: '20px', padding: '20px', background: '#ffe6e6', borderRadius: '8px' }}>
            <h3>❌ Bad: Width Animation (Layout Thrashing)</h3>
            <p>⚠️ This animates width - causes reflow/repaint on every frame!</p>
            
            {/* This is intentionally bad for demonstration */}
            <div style={{ 
              width: '100%', 
              height: '8px', 
              backgroundColor: 'rgba(255,0,0,0.2)', 
              borderRadius: '4px',
              overflow: 'hidden'
            }}>
              <motion.div
                style={{
                  height: '100%',
                  background: 'linear-gradient(90deg, #ff6b6b 0%, #ee5a24 100%)',
                  borderRadius: '4px'
                }}
                animate={{ width: `${percentage}%` }}
                transition={{ duration: 0.1, ease: "linear" }}
              />
            </div>
            <p style={{ color: '#d32f2f' }}>
              Check DevTools Performance tab - you'll see purple layout bars!
            </p>
          </div>
        )}
      </div>

      {/* Performance Variants Demo */}
      <div style={{ marginBottom: '30px' }}>
        <h3>🎨 Optimized Animation Variants</h3>
        
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
          <motion.div
            variants={PERFORMANCE_VARIANTS.fadeInOptimized}
            initial="hidden"
            animate="visible"
            style={{
              padding: '20px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              borderRadius: '8px',
              color: 'white',
              textAlign: 'center'
            }}
          >
            Fade In Optimized
          </motion.div>

          <motion.div
            variants={PERFORMANCE_VARIANTS.slideInOptimized}
            initial="hidden"
            animate="visible"
            style={{
              padding: '20px',
              background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
              borderRadius: '8px',
              color: 'white',
              textAlign: 'center'
            }}
          >
            Slide In Optimized
          </motion.div>

          <motion.div
            variants={PERFORMANCE_VARIANTS.scaleInOptimized}
            initial="hidden"
            animate="visible"
            style={{
              padding: '20px',
              background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
              borderRadius: '8px',
              color: 'white',
              textAlign: 'center'
            }}
          >
            Scale In Optimized
          </motion.div>
        </div>
      </div>

      {/* Device Configuration Info */}
      <div style={{ marginBottom: '30px' }}>
        <h3>📱 Device Configuration</h3>
        <div style={{ 
          padding: '15px', 
          background: '#f5f5f5', 
          borderRadius: '8px',
          fontFamily: 'monospace'
        }}>
          <pre>{JSON.stringify(config, null, 2)}</pre>
        </div>
      </div>

      {/* Performance Tips */}
      <div>
        <h3>💡 Performance Tips</h3>
        <ul>
          <li>✅ Always use <code>transform</code> and <code>opacity</code> for animations</li>
          <li>✅ Add <code>translateZ(0)</code> to force GPU acceleration</li>
          <li>✅ Use <code>willChange</code> only during animation</li>
          <li>❌ Avoid animating <code>width</code>, <code>height</code>, <code>margin</code>, <code>padding</code></li>
          <li>❌ Don't leave <code>willChange</code> applied permanently</li>
          <li>🔧 Test on low-power devices regularly</li>
          <li>🔧 Use Chrome DevTools Performance tab to validate 60fps</li>
        </ul>
      </div>
    </div>
  );
};

export default PerformanceDemo; 