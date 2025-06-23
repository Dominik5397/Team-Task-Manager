/**
 * Global animation configuration
 * Centralized settings for consistent animation timing and behavior
 */

// Detect user preferences
export const prefersReducedMotion = typeof window !== 'undefined' 
  ? window.matchMedia('(prefers-reduced-motion: reduce)').matches 
  : false;

// Detect device capabilities (simplified heuristic)
export const isLowPowerDevice = typeof navigator !== 'undefined' 
  ? navigator.hardwareConcurrency <= 4 
  : false;

// Animation durations (in seconds)
export const DURATION = {
  FAST: 0.2,
  NORMAL: 0.3,
  SLOW: 0.5,
  VERY_SLOW: 0.8
} as const;

// Responsive durations based on user preferences and device
export const getAnimationDuration = (baseDuration: number): number => {
  if (prefersReducedMotion) return 0;
  if (isLowPowerDevice) return baseDuration * 0.7;
  return baseDuration;
};

// Spring configurations
export const SPRING = {
  SOFT: { type: "spring", stiffness: 200, damping: 25 },
  MEDIUM: { type: "spring", stiffness: 300, damping: 25 },
  STIFF: { type: "spring", stiffness: 400, damping: 30 },
  BOUNCE: { type: "spring", stiffness: 300, damping: 15 }
} as const;

// Easing functions
export const EASING = {
  EASE_OUT: "easeOut",
  EASE_IN: "easeIn",
  EASE_IN_OUT: "easeInOut",
  LINEAR: "linear"
} as const;

// Stagger timing for list animations
export const STAGGER = {
  FAST: 0.05,
  NORMAL: 0.1,
  SLOW: 0.15
} as const;

// Scale values for interactive elements
export const SCALE = {
  HOVER: 1.02,
  HOVER_BUTTON: 1.05,
  TAP: 0.98,
  TAP_BUTTON: 0.95,
  MODAL: 0.8
} as const;

// Transform values for slide animations
export const SLIDE = {
  SMALL: 20,
  MEDIUM: 50,
  LARGE: 100
} as const;

// Animation configuration factory
export const createAnimationConfig = (options: {
  duration?: number;
  spring?: boolean;
  reduced?: boolean;
}) => {
  const { duration = DURATION.NORMAL, spring = false, reduced = false } = options;
  
  const actualDuration = reduced || prefersReducedMotion ? 0 : getAnimationDuration(duration);
  
  if (spring && actualDuration > 0) {
    return SPRING.MEDIUM;
  }
  
  return {
    duration: actualDuration,
    ease: EASING.EASE_OUT
  };
};

// Global animation settings
export const ANIMATION_CONFIG = {
  // Whether animations are enabled globally
  enabled: !prefersReducedMotion,
  
  // Default transition for most animations
  defaultTransition: createAnimationConfig({ duration: DURATION.NORMAL }),
  
  // Transition for layout changes
  layoutTransition: createAnimationConfig({ duration: DURATION.FAST }),
  
  // Transition for page changes
  pageTransition: createAnimationConfig({ duration: DURATION.SLOW }),
  
  // Transition for micro-interactions
  microTransition: createAnimationConfig({ duration: DURATION.FAST }),
  
  // Whether to use spring animations by default
  useSpring: !isLowPowerDevice,
  
  // Reduced motion fallbacks
  reducedMotion: {
    duration: 0,
    ease: EASING.LINEAR
  }
} as const;

// Breakpoint-specific animation configs
export const RESPONSIVE_CONFIG = {
  mobile: {
    duration: DURATION.FAST,
    stagger: STAGGER.FAST,
    enabled: !prefersReducedMotion && !isLowPowerDevice
  },
  tablet: {
    duration: DURATION.NORMAL,
    stagger: STAGGER.NORMAL,
    enabled: !prefersReducedMotion
  },
  desktop: {
    duration: DURATION.NORMAL,
    stagger: STAGGER.NORMAL,
    enabled: !prefersReducedMotion
  }
} as const;

// Utility function to get current breakpoint config
export const getCurrentBreakpointConfig = () => {
  if (typeof window === 'undefined') return RESPONSIVE_CONFIG.desktop;
  
  const width = window.innerWidth;
  if (width < 768) return RESPONSIVE_CONFIG.mobile;
  if (width < 1024) return RESPONSIVE_CONFIG.tablet;
  return RESPONSIVE_CONFIG.desktop;
}; 