// Global variables
let currentLanguage = localStorage.getItem('language') || 'uz';

// Initialize AOS
AOS.init({
  duration: 1000,
  easing: 'ease-in-out-cubic',
  once: true,
  offset: 100,
  disable: 'mobile'
});

// Set current year
document.getElementById('currentYear').textContent = new Date().getFullYear();

// Theme Toggle Functionality
const themeToggle = document.getElementById('themeToggle');
const themeIcon = document.getElementById('themeIcon');
const html = document.documentElement;

// Check for saved theme preference or default to 'light'
const currentTheme = localStorage.getItem('theme') || 'light';
html.setAttribute('data-theme', currentTheme);
updateThemeIcon(currentTheme);

themeToggle.addEventListener('click', () => {
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

// Loading Screen
window.addEventListener('load', () => {
  const loadingScreen = document.getElementById('loadingScreen');
  setTimeout(() => {
    loadingScreen.classList.add('hidden');
    setTimeout(() => {
      loadingScreen.style.display = 'none';
    }, 500);
  }, 1500);
});

// Scroll to Top Button
const scrollTopBtn = document.getElementById('scrollTop');

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

// Header Scroll Effect
const mainHeader = document.getElementById('mainHeader');

window.addEventListener('scroll', throttle(() => {
  if (window.scrollY > 100) {
    mainHeader.classList.add('scrolled');
  } else {
    mainHeader.classList.remove('scrolled');
  }
}, 100));

// Load statistics from backend
async function loadStats() {
  try {
    const response = await fetch('/api/admin/stats');
    if (response.ok) {
      const result = await response.json();
      const stats = result.data;

      // Update hero stats
      updateHeroStats(stats);

      // Update about stats
      updateAboutStats(stats);
    }
  } catch (error) {
    console.error('Error loading stats:', error);
    // Use default values if API fails
    updateHeroStats({
      totalClients: 500,
      avgExperience: 15,
      successRate: 98,
      casesLast24h: 24
    });

    updateAboutStats({
      totalLawyers: 10,
      totalClients: 25,
      successfulCases: 150,
      avgExperience: 15
    });
  }
}

function updateHeroStats(stats) {
  const heroStatsContainer = document.getElementById('heroStats');
  if (!heroStatsContainer) return;

  const statsData = [
    { value: stats.totalClients || 500, label: currentLanguage === 'uz' ? 'Мижозлар' : currentLanguage === 'ru' ? 'Клиентов' : 'Clients' },
    { value: Math.round(stats.avgExperience) || 15, label: currentLanguage === 'uz' ? 'Йиллик тажриба' : currentLanguage === 'ru' ? 'Лет опыта' : 'Years Experience' },
    { value: Math.round(stats.successRate) || 98, label: currentLanguage === 'uz' ? 'Муваффақият %' : currentLanguage === 'ru' ? 'Успех %' : 'Success %' },
    { value: '24/7', label: currentLanguage === 'uz' ? 'Хизмат' : currentLanguage === 'ru' ? 'Сервис' : 'Service' }
  ];

  heroStatsContainer.innerHTML = statsData.map(stat => `
    <div class="hero-stat">
      <span class="hero-stat-number" data-count="${typeof stat.value === 'number' ? stat.value : 0}">${stat.value}</span>
      <span class="hero-stat-label">${stat.label}</span>
    </div>
  `).join('');

  // Animate counters
  setTimeout(() => animateCounters(), 500);
}

function updateAboutStats(stats) {
  const aboutStatsContainer = document.getElementById('aboutStats');
  if (!aboutStatsContainer) return;

  const statsData = [
    {
      value: stats.totalLawyers || 10,
      label: currentLanguage === 'uz' ? 'Адвокатлар' : currentLanguage === 'ru' ? 'Адвокатов' : 'Lawyers',
      description: currentLanguage === 'uz' ? 'Профессионал адвокатлар жамоаси' : currentLanguage === 'ru' ? 'Команда профессиональных адвокатов' : 'Team of professional lawyers'
    },
    {
      value: stats.totalClients || 25,
      label: currentLanguage === 'uz' ? 'Бизнинг мижозлар' : currentLanguage === 'ru' ? 'Наших клиентов' : 'Our Clients',
      description: currentLanguage === 'uz' ? 'Доимий ҳамкорлик қилувчи мижозлар' : currentLanguage === 'ru' ? 'Постоянно сотрудничающие клиенты' : 'Constantly cooperating clients'
    },
    {
      value: stats.successfulCases || 150,
      label: currentLanguage === 'uz' ? 'Муваффақиятли натижалар' : currentLanguage === 'ru' ? 'Успешных результатов' : 'Successful Results',
      description: currentLanguage === 'uz' ? 'Ечилган юридик масалалар' : currentLanguage === 'ru' ? 'Решенных юридических вопросов' : 'Resolved legal issues'
    },
    {
      value: Math.round(stats.avgExperience) || 15,
      label: currentLanguage === 'uz' ? 'Иш тажрибаси' : currentLanguage === 'ru' ? 'Опыт работы' : 'Work Experience',
      description: currentLanguage === 'uz' ? 'Йиллик профессионал тажриба' : currentLanguage === 'ru' ? 'Лет профессионального опыта' : 'Years of professional experience'
    }
  ];

  aboutStatsContainer.innerHTML = statsData.map((stat, index) => `
    <div class="stat-card" data-aos="fade-up" data-aos-delay="${(index + 1) * 100}">
      <span class="stat-number" data-count="${stat.value}">0</span>
      <div class="stat-label">${stat.label}</div>
      <div class="stat-description">${stat.description}</div>
    </div>
  `).join('');

  // Animate counters
  setTimeout(() => animateCounters(), 200);
}

// Load services from backend or use static data
async function loadServices() {
  const servicesContainer = document.getElementById('servicesGrid');
  if (!servicesContainer) return;

  const servicesData = [
    {
      icon: 'fas fa-building',
      titleKey: 'services.corporate',
      features: [
        currentLanguage === 'uz' ? 'Компания ташкил этиш' : currentLanguage === 'ru' ? 'Создание компаний' : 'Company formation',
        currentLanguage === 'uz' ? 'Шартномалар тузиш' : currentLanguage === 'ru' ? 'Составление договоров' : 'Contract drafting',
        currentLanguage === 'uz' ? 'Корпоратив бошқарув' : currentLanguage === 'ru' ? 'Корпоративное управление' : 'Corporate governance',
        currentLanguage === 'uz' ? 'M&A операциялари' : currentLanguage === 'ru' ? 'M&A операции' : 'M&A operations'
      ]
    },
    {
      icon: 'fas fa-users',
      titleKey: 'services.civil',
      features: [
        currentLanguage === 'uz' ? 'Мулкий низолар' : currentLanguage === 'ru' ? 'Имущественные споры' : 'Property disputes',
        currentLanguage === 'uz' ? 'Шартномавий низолар' : currentLanguage === 'ru' ? 'Договорные споры' : 'Contract disputes',
        currentLanguage === 'uz' ? 'Зарар қоплаш' : currentLanguage === 'ru' ? 'Возмещение ущерба' : 'Damage compensation',
        currentLanguage === 'uz' ? 'Мерос масалалари' : currentLanguage === 'ru' ? 'Наследственные вопросы' : 'Inheritance matters'
      ]
    },
    {
      icon: 'fas fa-gavel',
      titleKey: 'services.criminal',
      features: [
        currentLanguage === 'uz' ? 'Жиноят ишларида ҳимоя' : currentLanguage === 'ru' ? 'Защита по уголовным делам' : 'Criminal defense',
        currentLanguage === 'uz' ? 'Жабрланувчилар ҳуқуқи' : currentLanguage === 'ru' ? 'Права потерпевших' : 'Victims\' rights',
        currentLanguage === 'uz' ? 'Апелляция шикоятлари' : currentLanguage === 'ru' ? 'Апелляционные жалобы' : 'Appeal complaints',
        currentLanguage === 'uz' ? 'Юридик маслаҳат' : currentLanguage === 'ru' ? 'Юридические консультации' : 'Legal consultation'
      ]
    },
    {
      icon: 'fas fa-heart',
      titleKey: 'services.family',
      features: [
        currentLanguage === 'uz' ? 'Ажрашиш жараёни' : currentLanguage === 'ru' ? 'Процедура развода' : 'Divorce proceedings',
        currentLanguage === 'uz' ? 'Болалар ҳуқуқи' : currentLanguage === 'ru' ? 'Права детей' : 'Children\'s rights',
        currentLanguage === 'uz' ? 'Алимент масалалари' : currentLanguage === 'ru' ? 'Алиментные вопросы' : 'Alimony matters',
        currentLanguage === 'uz' ? 'Мулкни бўлиш' : currentLanguage === 'ru' ? 'Раздел имущества' : 'Property division'
      ]
    },
    {
      icon: 'fas fa-briefcase',
      titleKey: 'services.business',
      features: [
        currentLanguage === 'uz' ? 'Лицензия олиш' : currentLanguage === 'ru' ? 'Получение лицензий' : 'License obtaining',
        currentLanguage === 'uz' ? 'Рухсатномалар' : currentLanguage === 'ru' ? 'Разрешения' : 'Permits',
        currentLanguage === 'uz' ? 'Бизнес режалаштириш' : currentLanguage === 'ru' ? 'Бизнес-планирование' : 'Business planning',
        currentLanguage === 'uz' ? 'Инвестиция ҳуқуқи' : currentLanguage === 'ru' ? 'Инвестиционное право' : 'Investment law'
      ]
    },
    {
      icon: 'fas fa-calculator',
      titleKey: 'services.tax',
      features: [
        currentLanguage === 'uz' ? 'Солиқ маслаҳати' : currentLanguage === 'ru' ? 'Налоговые консультации' : 'Tax consultation',
        currentLanguage === 'uz' ? 'Солиқ низолари' : currentLanguage === 'ru' ? 'Налоговые споры' : 'Tax disputes',
        currentLanguage === 'uz' ? 'Солиқ текшируви' : currentLanguage === 'ru' ? 'Налоговые проверки' : 'Tax audits',
        currentLanguage === 'uz' ? 'Солиқ оптимизацияси' : currentLanguage === 'ru' ? 'Налоговая оптимизация' : 'Tax optimization'
      ]
    }
  ];

  servicesContainer.innerHTML = servicesData.map((service, index) => `
    <div class="service-card" data-aos="fade-up" data-aos-delay="${(index + 1) * 100}">
      <div class="service-icon">
        <i class="${service.icon}"></i>
      </div>
      <h3 class="service-title">${t(service.titleKey)}</h3>
      <p class="service-description">
        ${getServiceDescription(service.titleKey)}
      </p>
      <ul class="service-features">
        ${service.features.map(feature => `
          <li><i class="fas fa-check"></i>${feature}</li>
        `).join('')}
      </ul>
    </div>
  `).join('');
}

function getServiceDescription(titleKey) {
  const descriptions = {
    'services.corporate': {
      uz: 'Компанияларни ташкил этиш, корпоратив бошқарув, шартномалар тузиш ва бизнес жараёнларини юридик жиҳатдан қўллаб-қувватлаш.',
      ru: 'Создание компаний, корпоративное управление, составление договоров и юридическая поддержка бизнес-процессов.',
      en: 'Company formation, corporate governance, contract drafting and legal support of business processes.'
    },
    'services.civil': {
      uz: 'Фуқаролик ҳуқуқи соҳасидаги низолар, мулкий муносабатлар, шартномавий мажбуриятлар ва бошқа фуқаролик ишлари.',
      ru: 'Споры в области гражданского права, имущественные отношения, договорные обязательства и другие гражданские дела.',
      en: 'Disputes in civil law, property relations, contractual obligations and other civil matters.'
    },
    'services.criminal': {
      uz: 'Жиноят ишларида ҳимоя, жабрланувчиларнинг ҳуқуқларини ҳимоя қилиш ва жиноят процессида юридик ёрдам кўрсатиш.',
      ru: 'Защита по уголовным делам, защита прав потерпевших и оказание юридической помощи в уголовном процессе.',
      en: 'Defense in criminal cases, protection of victims\' rights and legal assistance in criminal proceedings.'
    },
    'services.family': {
      uz: 'Никоҳ ва ажрашиш масалалари, болаларнинг ҳуқуқлари, алимент тўлаш ва оилавий низоларни ҳал қилиш.',
      ru: 'Вопросы брака и развода, права детей, выплата алиментов и разрешение семейных споров.',
      en: 'Marriage and divorce issues, children\'s rights, alimony payments and family dispute resolution.'
    },
    'services.business': {
      uz: 'Тадбиркорлик фаолияти, лицензиялаш, рухсатнома олиш ва бизнес жараёнларини юридик жиҳатдан қўллаб-қувватлаш.',
      ru: 'Предпринимательская деятельность, лицензирование, получение разрешений и юридическая поддержка бизнес-процессов.',
      en: 'Entrepreneurial activities, licensing, obtaining permits and legal support of business processes.'
    },
    'services.tax': {
      uz: 'Солиқ қонунчилиги бўйича маслаҳат, солиқ низолари, солиқ текшируви ва солиқ оптимизацияси масалалари.',
      ru: 'Консультации по налоговому законодательству, налоговые споры, налоговые проверки и вопросы налоговой оптимизации.',
      en: 'Tax legislation consultation, tax disputes, tax audits and tax optimization issues.'
    }
  };

  return descriptions[titleKey]?.[currentLanguage] || '';
}

// Counter Animation
function animateCounters() {
  const counters = document.querySelectorAll('[data-count]');

  counters.forEach(counter => {
    const target = parseInt(counter.getAttribute('data-count'));
    if (isNaN(target)) return;

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

// Form handling
document.addEventListener('DOMContentLoaded', function() {
  const form = document.getElementById('contactForm');
  const submitBtn = document.getElementById('submitBtn');
  const successAlert = document.getElementById('successAlert');
  const errorAlert = document.getElementById('errorAlert');

  // Load initial data
  loadStats();
  loadServices();

  // Observe counter sections
  const heroStats = document.querySelector('.hero-stats');
  const statsGrid = document.querySelector('.stats-grid');

  if (heroStats) observer.observe(heroStats);
  if (statsGrid) observer.observe(statsGrid);

  // Form validation and submission
  if (form) {
    form.addEventListener('submit', async function(e) {
      e.preventDefault();
      e.stopPropagation();

      // Hide previous alerts
      successAlert.classList.add('d-none');
      errorAlert.classList.add('d-none');

      // Validate form
      if (!form.checkValidity()) {
        form.classList.add('was-validated');
        return;
      }

      // Show loading state
      submitBtn.classList.add('btn-loading');
      submitBtn.disabled = true;

      // Collect form data
      const formData = {
        name: document.getElementById('name').value.trim(),
        email: document.getElementById('email').value.trim(),
        phone: document.getElementById('phone').value.trim(),
        serviceType: document.getElementById('serviceType').value,
        clientType: document.getElementById('clientType').value,
        subject: document.getElementById('subject').value.trim(),
        message: document.getElementById('message').value.trim(),
        langCode: currentLanguage
      };

      try {
        // Send to backend API
        const response = await fetch('/api/contact', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          body: JSON.stringify(formData)
        });

        if (response.ok) {
          // Success
          successAlert.classList.remove('d-none');
          form.reset();
          form.classList.remove('was-validated');

          // Scroll to success message
          successAlert.scrollIntoView({ behavior: 'smooth', block: 'center' });

          // Hide success message after 5 seconds
          setTimeout(() => {
            successAlert.classList.add('d-none');
          }, 5000);
        } else {
          throw new Error('Server error');
        }
      } catch (error) {
        console.error('Error:', error);
        errorAlert.classList.remove('d-none');
        errorAlert.scrollIntoView({ behavior: 'smooth', block: 'center' });

        // Hide error message after 5 seconds
        setTimeout(() => {
          errorAlert.classList.add('d-none');
        }, 5000);
      } finally {
        // Remove loading state
        submitBtn.classList.remove('btn-loading');
        submitBtn.disabled = false;
      }
    });

    // Real-time validation
    const inputs = form.querySelectorAll('input[required], textarea[required]');
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

      if (value === '') {
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
      } else {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
      }
    }

    // Phone number formatting
    // JavaScript qismi
    const phoneInput = document.getElementById('phone');

    if (phoneInput) {
      // +998 bilan boshlanishini majburiy qilamiz
      phoneInput.addEventListener('input', function () {
        if (!this.value.startsWith('+998')) {
          this.value = '+998';
        }

        // faqat raqamlar bo'lishi kerak (+998 dan keyingi qism)
        let digits = this.value.replace(/\D/g, '');
        if (digits.length > 12) {
          // ortiqcha raqam kiritilmasligi uchun kesib olamiz
          this.value = '+' + digits.substring(0, 12);
        }
      });

      // kursorni +998 oldida harakatlantirishni bloklaymiz
      phoneInput.addEventListener('keydown', function (e) {
        const start = this.selectionStart;
        if ((start <= 4) && (e.key === 'Backspace' || e.key === 'ArrowLeft')) {
          e.preventDefault();
          this.setSelectionRange(this.value.length, this.value.length);
        }
      });

      // inputga bosilganda kursor +998 dan keyin turishini ta'minlaymiz
      phoneInput.addEventListener('focus', function () {
        setTimeout(() => {
          if (this.selectionStart < 4) {
            this.setSelectionRange(this.value.length, this.value.length);
          }
        }, 0);
      });
    }

  }

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

  // Ripple effect for buttons
  const buttons = document.querySelectorAll('.btn');
  buttons.forEach(button => {
    button.addEventListener('click', function(e) {
      createRipple(this, e);
    });
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
});

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

// Performance monitoring
window.addEventListener('load', () => {
  if ('performance' in window) {
    const perfData = performance.getEntriesByType('navigation')[0];
    console.log('Page load time:', perfData.loadEventEnd - perfData.loadEventStart, 'ms');
  }
});

// Error handling
window.addEventListener('error', (e) => {
  console.error('Global error:', e.error);
});

window.addEventListener('unhandledrejection', (e) => {
  console.error('Unhandled promise rejection:', e.reason);
});
