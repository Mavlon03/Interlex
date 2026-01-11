// Main JavaScript functionality
document.addEventListener('DOMContentLoaded', function() {
  // Initialize AOS
  if (typeof AOS !== 'undefined') {
    AOS.init({
      duration: 1000,
      easing: 'ease-in-out-cubic',
      once: true,
      offset: 100,
      disable: 'mobile'
    });
  }

  // Set current year
  const currentYearElement = document.getElementById('currentYear');
  if (currentYearElement) {
    currentYearElement.textContent = new Date().getFullYear();
  }

  // Theme Toggle Functionality
  initThemeToggle();

  // Loading Screen
  initLoadingScreen();

  // Scroll to Top Button
  initScrollToTop();

  // Header Scroll Effect
  initHeaderScrollEffect();

  // Counter Animation
  initCounterAnimation();

  // Form handling
  initContactForm();

  // Navigation
  initNavigation();

  // Utility functions
  initUtilityFunctions();

  // Performance monitoring
  initPerformanceMonitoring();

  // Error handling
  initErrorHandling();
});

// Theme Toggle Functionality
function initThemeToggle() {
  const themeToggle = document.getElementById('themeToggle');
  const themeIcon = document.getElementById('themeIcon');
  const html = document.documentElement;

  if (!themeToggle || !themeIcon) return;

  // Check for saved theme preference or default to 'light'
  const currentTheme = localStorage.getItem('theme') || 'light';
  html.setAttribute('data-theme', currentTheme);
  updateThemeIcon(currentTheme);

  themeToggle.addEventListener('click', (event) => {
    const currentTheme = html.getAttribute('data-theme');
    const newTheme = currentTheme === 'light' ? 'dark' : 'light';

    html.setAttribute('data-theme', newTheme);
    localStorage.setItem('theme', newTheme);
    updateThemeIcon(newTheme);

    // Add ripple effect
    createRipple(themeToggle, event);
  });

  function updateThemeIcon(theme) {
    if (theme === 'dark') {
      themeIcon.className = 'fas fa-sun';
      themeToggle.title = 'Light Mode';
    } else {
      themeIcon.className = 'fas fa-moon';
      themeToggle.title = 'Dark Mode';
    }
  }
}

// Loading Screen
function initLoadingScreen() {
  window.addEventListener('load', () => {
    const loadingScreen = document.getElementById('loadingScreen');
    if (loadingScreen) {
      setTimeout(() => {
        loadingScreen.classList.add('hidden');
        setTimeout(() => {
          loadingScreen.style.display = 'none';
        }, 500);
      }, 1500);
    }
  });
}

// Scroll to Top Button
function initScrollToTop() {
  const scrollTopBtn = document.getElementById('scrollTop');
  if (!scrollTopBtn) return;

  window.addEventListener('scroll', throttle(() => {
    if (window.pageYOffset > 300) {
      scrollTopBtn.classList.add('visible');
    } else {
      scrollTopBtn.classList.remove('visible');
    }
  }, 100));

  scrollTopBtn.addEventListener('click', () => {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  });
}

// Header Scroll Effect
function initHeaderScrollEffect() {
  const mainHeader = document.getElementById('mainHeader');
  if (!mainHeader) return;

  window.addEventListener('scroll', throttle(() => {
    if (window.scrollY > 100) {
      mainHeader.classList.add('scrolled');
    } else {
      mainHeader.classList.remove('scrolled');
    }
  }, 100));
}

// Counter Animation
function initCounterAnimation() {
  function animateCounters() {
    const counters = document.querySelectorAll('[data-count]');

    counters.forEach(counter => {
      const target = parseInt(counter.getAttribute('data-count'));
      const increment = target / 100;
      let current = 0;

      const updateCounter = () => {
        if (current < target) {
          current += increment;
          counter.textContent = Math.ceil(current);
          requestAnimationFrame(updateCounter);
        } else {
          counter.textContent = target;
        }
      };

      updateCounter();
    });
  }

  // Intersection Observer for counters
  const observerOptions = {
    threshold: 0.5,
    rootMargin: '0px 0px -100px 0px'
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        if (entry.target.classList.contains('hero-stats')) {
          setTimeout(animateCounters, 500);
        } else if (entry.target.classList.contains('stats-grid')) {
          setTimeout(animateCounters, 200);
        }
        observer.unobserve(entry.target);
      }
    });
  }, observerOptions);

  // Observe counter sections
  const heroStats = document.querySelector('.hero-stats');
  const statsGrid = document.querySelector('.stats-grid');

  if (heroStats) observer.observe(heroStats);
  if (statsGrid) observer.observe(statsGrid);
}

// Contact Form with Axios
function initContactForm() {
  const form = document.getElementById('contactForm');
  const submitBtn = document.getElementById('submitBtn');
  const successAlert = document.getElementById('successAlert');
  const errorAlert = document.getElementById('errorAlert');

  if (!form) return;

  // Form validation and submission
  form.addEventListener('submit', async function(e) {
    e.preventDefault();
    e.stopPropagation();

    // Hide previous alerts
    if (successAlert) successAlert.classList.add('d-none');
    if (errorAlert) errorAlert.classList.add('d-none');

    // Validate form
    if (!form.checkValidity()) {
      form.classList.add('was-validated');
      return;
    }

    // Show loading state
    if (submitBtn) {
      submitBtn.classList.add('btn-loading');
      submitBtn.disabled = true;
    }

    // Get current language
    const currentLang = window.i18n ? window.i18n.getCurrentLanguage() : 'uz';

    // Collect form data according to backend DTO
    const formData = {
      name: document.getElementById('name')?.value.trim(),
      email: document.getElementById('email')?.value.trim(),
      phone: document.getElementById('phone')?.value.trim(),
      serviceType: document.getElementById('serviceType')?.value || null,
      clientType: document.getElementById('clientType')?.value || null,
      subject: document.getElementById('subject')?.value.trim(),
      message: document.getElementById('message')?.value.trim(),
      langCode: currentLang
    };

    try {
      const response = await axios.post('https://maxlegal.uz/api/contact', formData, {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      });

      const result = response.data;

      if (response.status === 200 && result.success) {
        // Success
        if (successAlert) {
          const successMessage = successAlert.querySelector('[data-i18n="contact.success"]');
          if (successMessage && result.message) {
            successMessage.textContent = result.message;
          }
          successAlert.classList.remove('d-none');
          successAlert.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }

        form.reset();
        form.classList.remove('was-validated');

        // Hide success message after 8 seconds
        setTimeout(() => {
          if (successAlert) successAlert.classList.add('d-none');
        }, 8000);
      } else {
        throw new Error(result.message || 'Server error');
      }
    } catch (error) {
      console.error('Error:', error);
      if (errorAlert) {
        const errorMessage = errorAlert.querySelector('[data-i18n="contact.error"]');
        if (errorMessage) {
          if (error.response && error.response.data && error.response.data.message) {
            errorMessage.textContent = error.response.data.message;
          } else {
            errorMessage.textContent = error.message || 'Server error';
          }
        }
        errorAlert.classList.remove('d-none');
        errorAlert.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }

      // Hide error message after 8 seconds
      setTimeout(() => {
        if (errorAlert) errorAlert.classList.add('d-none');
      }, 8000);
    } finally {
      // Remove loading state
      if (submitBtn) {
        submitBtn.classList.remove('btn-loading');
        submitBtn.disabled = false;
      }
    }
  });

  // Real-time validation
  const inputs = form.querySelectorAll('input[required], textarea[required], select[required]');
  inputs.forEach(input => {
    input.addEventListener('blur', function() {
      validateField(this);
    });

    input.addEventListener('input', function() {
      if (this.classList.contains('is-invalid') && this.value.trim() !== '') {
        validateField(this);
      }
    });
  });

  function validateField(field) {
    const value = field.value.trim();

    if (field.hasAttribute('required') && value === '') {
      field.classList.add('is-invalid');
      field.classList.remove('is-valid');
    } else if (field.type === 'email') {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (emailRegex.test(value)) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
      } else {
        field.classList.add('is-invalid');
        field.classList.remove('is-valid');
      }
    } else if (field.type === 'tel') {
      // Phone validation - should start with +998
      const phoneRegex = /^\+998\d{9}$/;
      if (phoneRegex.test(value)) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
      } else {
        field.classList.add('is-invalid');
        field.classList.remove('is-valid');
      }
    } else {
      field.classList.remove('is-invalid');
      field.classList.add('is-valid');
    }
  }

  // Phone number formatting
  const phoneInput = document.getElementById('phone');
  if (phoneInput) {
    phoneInput.addEventListener('input', function() {
      let value = this.value.replace(/\D/g, '');

      // Auto-format to +998 format
      if (value.startsWith('998')) {
        value = '+' + value;
      } else if (value.startsWith('99')) {
        value = '+9' + value;
      } else if (value.startsWith('9')) {
        value = '+99' + value;
      } else if (value.length > 0 && !value.startsWith('998')) {
        value = '+998' + value;
      }

      // Limit to +998XXXXXXXXX format (13 characters)
      if (value.length > 13) {
        value = value.substring(0, 13);
      }

      this.value = value;
    });

    // Validate on blur
    phoneInput.addEventListener('blur', function() {
      const value = this.value.trim();
      if (value && !value.match(/^\+998\d{9}$/)) {
        this.classList.add('is-invalid');
        this.classList.remove('is-valid');
      }
    });
  }

  // Form field animations
  const formControls = document.querySelectorAll('.form-control, .form-select');
  formControls.forEach(control => {
    control.addEventListener('focus', function() {
      this.parentElement.classList.add('focused');
    });

    control.addEventListener('blur', function() {
      if (this.value === '') {
        this.parentElement.classList.remove('focused');
      }
    });

    // Check if field has value on page load
    if (control.value !== '') {
      control.parentElement.classList.add('focused');
    }
  });
}

// Navigation
function initNavigation() {
  // Smooth scrolling for navigation links
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(e) {
      e.preventDefault();
      const target = document.querySelector(this.getAttribute('href'));
      if (target) {
        const headerOffset = 80;
        const elementPosition = target.getBoundingClientRect().top;
        const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

        window.scrollTo({
          top: offsetPosition,
          behavior: 'smooth'
        });
      }
    });
  });

  // Navbar active link update
  const navLinks = document.querySelectorAll('.navbar-nav .nav-link[href^="#"]');
  const sections = document.querySelectorAll('section[id]');

  window.addEventListener('scroll', throttle(() => {
    let current = '';
    sections.forEach(section => {
      const sectionTop = section.getBoundingClientRect().top;
      if (sectionTop <= 150) {
        current = section.getAttribute('id');
      }
    });

    navLinks.forEach(link => {
      link.classList.remove('active');
      if (link.getAttribute('href') === `#${current}`) {
        link.classList.add('active');
      }
    });
  }, 100));

  // Typing effect for hero title
  const heroTitle = document.getElementById('heroTitle');
  if (heroTitle) {
    const text = heroTitle.textContent;
    heroTitle.textContent = '';
    let i = 0;

    setTimeout(() => {
      const typeWriter = () => {
        if (i < text.length) {
          heroTitle.textContent += text.charAt(i);
          i++;
          setTimeout(typeWriter, 50);
        }
      };
      typeWriter();
    }, 2000);
  }
}

// Utility Functions
function initUtilityFunctions() {
  // Ripple effect for buttons
  const buttons = document.querySelectorAll('.btn');
  buttons.forEach(button => {
    button.addEventListener('click', function(e) {
      createRipple(this, e);
    });
  });

  // Preload critical images
  const criticalImages = [
    'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-HxAIhrT2W99aFFQVBgRXqxq3FxXgph.png',
    'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/image-0aEZYAUu66SEf8lorGQ5IvVaQDHVl0.png'
  ];


  criticalImages.forEach(src => {
    const img = new Image();
    img.src = src;
  });

  // Keyboard navigation support
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      // Close any open dropdowns
      const openDropdowns = document.querySelectorAll('.dropdown-menu.show');
      openDropdowns.forEach(dropdown => {
        dropdown.classList.remove('show');
      });
    }
  });

  // Initialize tooltips
  if (typeof bootstrap !== 'undefined') {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
      return new bootstrap.Tooltip(tooltipTriggerEl);
    });
  }
}

// Performance Monitoring
function initPerformanceMonitoring() {
  window.addEventListener('load', () => {
    if ('performance' in window) {
      const perfData = performance.getEntriesByType('navigation')[0];
      console.log('Page load time:', perfData.loadEventEnd - perfData.loadEventStart, 'ms');
    }
  });
}

// Error Handling
function initErrorHandling() {
  window.addEventListener('error', (e) => {
    console.error('Global error:', e.error);
  });

  window.addEventListener('unhandledrejection', (e) => {
    console.error('Unhandled promise rejection:', e.reason);
  });
}

// Utility Functions
function throttle(func, limit) {
  let inThrottle;
  return function() {
    const args = arguments;
    const context = this;
    if (!inThrottle) {
      func.apply(context, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  }
}

function createRipple(element, event) {
  const ripple = document.createElement('span');
  const rect = element.getBoundingClientRect();
  const size = Math.max(rect.width, rect.height);
  const x = event.clientX - rect.left - size / 2;
  const y = event.clientY - rect.top - size / 2;

  ripple.style.width = ripple.style.height = size + 'px';
  ripple.style.left = x + 'px';
  ripple.style.top = y + 'px';
  ripple.classList.add('ripple');

  element.appendChild(ripple);

  setTimeout(() => {
    ripple.remove();
  }, 600);
}

// Export functions for global use
window.createRipple = createRipple;
window.throttle = throttle;