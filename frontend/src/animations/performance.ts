/**
 * Animation Performance Optimization Module
 * Tools and utilities for 60fps animations using GPU-accelerated properties
 */

// Performance monitoring utilities
export class AnimationPerformanceMonitor {
  private static instance: AnimationPerformanceMonitor;
  private observers: PerformanceObserver[] = [];
  private animationMetrics: Map<string, number[]> = new Map();

  static getInstance(): AnimationPerformanceMonitor {
    if (!AnimationPerformanceMonitor.instance) {
      AnimationPerformanceMonitor.instance = new AnimationPerformanceMonitor();
    }
    return AnimationPerformanceMonitor.instance;
  }

  startMonitoring(animationName: string): void {
    if (typeof window === 'undefined' || !window.performance) return;
    
    // Monitor frame rate during animation
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.entryType === 'measure') {
            const duration = entry.duration;
            this.recordMetric(animationName, duration);
          }
        }
      });
      
      observer.observe({ entryTypes: ['measure'] });
      this.observers.push(observer);
    }
  }

  endMonitoring(animationName: string): void {
    if (typeof window === 'undefined' || !window.performance) return;
    
    performance.mark(`${animationName}-end`);
    performance.measure(animationName, `${animationName}-start`, `${animationName}-end`);
    
    // Clean up observers
    this.observers.forEach(observer => observer.disconnect());
    this.observers = [];
  }

  private recordMetric(animationName: string, duration: number): void {
    if (!this.animationMetrics.has(animationName)) {
      this.animationMetrics.set(animationName, []);
    }
    this.animationMetrics.get(animationName)!.push(duration);
  }

  getMetrics(animationName: string): { average: number; max: number; min: number } | null {
    const metrics = this.animationMetrics.get(animationName);
    if (!metrics || metrics.length === 0) return null;

    const average = metrics.reduce((sum, val) => sum + val, 0) / metrics.length;
    const max = Math.max(...metrics);
    const min = Math.min(...metrics);

    return { average, max, min };
  }

  getFrameRate(): number {
    if (typeof window === 'undefined') return 60;
    
    // Simplified frame rate detection
    let lastTime = performance.now();
    let frameCount = 0;
    let fps = 60;

    const calculateFPS = () => {
      const now = performance.now();
      frameCount++;
      
      if (now - lastTime >= 1000) {
        fps = Math.round((frameCount * 1000) / (now - lastTime));
        frameCount = 0;
        lastTime = now;
      }
      
      requestAnimationFrame(calculateFPS);
    };

    requestAnimationFrame(calculateFPS);
    return fps;
  }
}

// GPU-optimized animation utilities
export const GPU_OPTIMIZED = {
  // ✅ GOOD: GPU-accelerated properties
  SAFE_PROPERTIES: [
    'transform',
    'opacity',
    'filter'
  ],
  
  // ❌ BAD: Layout-affecting properties
  LAYOUT_PROPERTIES: [
    'width', 'height',
    'margin', 'padding',
    'left', 'right', 'top', 'bottom',
    'border-width',
    'font-size'
  ],

  // Transform helpers for common animations
  scale: (value: number) => `scale(${value})`,
  translate: (x: number, y: number = 0) => `translate(${x}px, ${y}px)`,
  translateX: (x: number) => `translateX(${x}px)`,
  translateY: (y: number) => `translateY(${y}px)`,
  rotate: (degrees: number) => `rotate(${degrees}deg)`,
  
  // Combined transforms
  scaleAndTranslate: (scale: number, x: number, y: number = 0) => 
    `scale(${scale}) translate(${x}px, ${y}px)`,
    
  // Optimized progress bar using transform instead of width
  progressTransform: (percentage: number) => 
    `scaleX(${percentage / 100}) translateZ(0)`,
    
  // 3D acceleration hack
  force3D: 'translateZ(0)',
} as const;

// Performance-optimized animation variants
export const PERFORMANCE_VARIANTS = {
  // Optimized fade with 3D acceleration
  fadeInOptimized: {
    hidden: { 
      opacity: 0,
      transform: GPU_OPTIMIZED.force3D
    },
    visible: { 
      opacity: 1,
      transform: GPU_OPTIMIZED.force3D,
      transition: { duration: 0.3, ease: "easeOut" }
    }
  },

  // Optimized slide using transform
  slideInOptimized: {
    hidden: { 
      opacity: 0,
      transform: GPU_OPTIMIZED.translate(-50, 0)
    },
    visible: { 
      opacity: 1,
      transform: GPU_OPTIMIZED.translate(0, 0),
      transition: { duration: 0.3, ease: "easeOut" }
    }
  },

  // Optimized scale with proper origin
  scaleInOptimized: {
    hidden: { 
      opacity: 0,
      transform: `${GPU_OPTIMIZED.scale(0.8)} ${GPU_OPTIMIZED.force3D}`
    },
    visible: { 
      opacity: 1,
      transform: `${GPU_OPTIMIZED.scale(1)} ${GPU_OPTIMIZED.force3D}`,
      transition: { duration: 0.3, ease: "easeOut" }
    }
  },

  // Optimized progress bar (no layout changes)
  progressBarOptimized: {
    initial: { 
      transform: GPU_OPTIMIZED.progressTransform(0)
    },
    animate: (percentage: number) => ({
      transform: GPU_OPTIMIZED.progressTransform(percentage),
      transition: { duration: 1.5, ease: "easeOut" }
    })
  }
} as const;

// Animation performance checker
export const validateAnimationProps = (props: Record<string, any>): {
  valid: boolean;
  warnings: string[];
} => {
  const warnings: string[] = [];
  let valid = true;

  // Check for layout-affecting properties
  Object.keys(props).forEach(prop => {
    if (GPU_OPTIMIZED.LAYOUT_PROPERTIES.includes(prop as any)) {
      warnings.push(`❌ Property "${prop}" causes layout recalculation. Use transform instead.`);
      valid = false;
    }
  });

  // Check for non-GPU properties
  Object.keys(props).forEach(prop => {
    if (!GPU_OPTIMIZED.SAFE_PROPERTIES.some(safe => prop.startsWith(safe))) {
      if (!GPU_OPTIMIZED.LAYOUT_PROPERTIES.includes(prop as any)) {
        warnings.push(`⚠️ Property "${prop}" may not be GPU-accelerated.`);
      }
    }
  });

  return { valid, warnings };
};

// Responsive animation configuration based on device capabilities
export const getDeviceOptimizedConfig = () => {
  if (typeof window === 'undefined') {
    return { duration: 0.3, reducedMotion: false };
  }

  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const isLowPowerDevice = navigator.hardwareConcurrency <= 4;
  const connection = (navigator as any).connection;
  
  // Check network speed
  const isSlowConnection = connection && (
    connection.effectiveType === 'slow-2g' || 
    connection.effectiveType === '2g' ||
    connection.saveData
  );

  return {
    duration: prefersReducedMotion ? 0 : isLowPowerDevice ? 0.2 : 0.3,
    reducedMotion: prefersReducedMotion,
    enableComplexAnimations: !isLowPowerDevice && !isSlowConnection,
    enable3D: !isLowPowerDevice,
    staggerDelay: isLowPowerDevice ? 0.05 : 0.1
  };
};

// Animation optimization hooks
export const useOptimizedAnimation = (animationName: string) => {
  const monitor = AnimationPerformanceMonitor.getInstance();
  const config = getDeviceOptimizedConfig();

  const startAnimation = () => {
    if (typeof window !== 'undefined' && window.performance) {
      performance.mark(`${animationName}-start`);
      monitor.startMonitoring(animationName);
    }
  };

  const endAnimation = () => {
    monitor.endMonitoring(animationName);
  };

  return {
    config,
    startAnimation,
    endAnimation,
    getMetrics: () => monitor.getMetrics(animationName)
  };
};

// Development-only performance debugging
export const DEBUG_PERFORMANCE = process.env.NODE_ENV === 'development' ? {
  logAnimationPerformance: (animationName: string, props: Record<string, any>) => {
    const validation = validateAnimationProps(props);
    console.group(`🎬 Animation: ${animationName}`);
    
    if (!validation.valid) {
      console.warn('Performance Issues Found:');
      validation.warnings.forEach(warning => console.warn(warning));
    } else {
      console.log('✅ Animation is GPU-optimized');
    }
    
    console.log('Properties:', props);
    console.groupEnd();
  },

  measureFrameRate: () => {
    const monitor = AnimationPerformanceMonitor.getInstance();
    console.log(`Current FPS: ${monitor.getFrameRate()}`);
  }
} : {
  logAnimationPerformance: () => {},
  measureFrameRate: () => {}
};

// Performance-optimized CSS-in-JS styles
export const OPTIMIZED_STYLES = {
  // Force GPU acceleration
  gpu: {
    transform: GPU_OPTIMIZED.force3D,
    backfaceVisibility: 'hidden' as const,
    perspective: 1000
  },

  // Optimized container for animations
  animationContainer: {
    position: 'relative' as const,
    transform: GPU_OPTIMIZED.force3D,
    willChange: 'transform, opacity' // Only when animating
  },

  // Progress bar container (prevents layout shift)
  progressContainer: {
    position: 'relative' as const,
    overflow: 'hidden' as const,
    borderRadius: '4px',
    background: 'rgba(255, 255, 255, 0.2)'
  },

  // Progress bar fill (uses transform instead of width)
  progressFill: {
    position: 'absolute' as const,
    top: 0,
    left: 0,
    height: '100%',
    width: '100%', // Always 100%, scaled with transform
    transformOrigin: 'left center',
    borderRadius: '4px'
  }
} as const; 