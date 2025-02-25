import { ref } from 'vue';

export const currentTheme = ref('light');

export function initTheme() {
    const theme = localStorage.getItem('theme');
    if (theme) {
        currentTheme.value = theme;
    } else if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
        currentTheme.value = 'dark';
    }

    if (currentTheme.value === 'dark') {
        document.documentElement.classList.add('dark');
    } else {
        document.documentElement.classList.remove('dark');
    }

    document.documentElement.setAttribute('data-theme', currentTheme.value);
    localStorage.setItem('theme', currentTheme.value);
}

export function switchTheme() {
    currentTheme.value = currentTheme.value === 'light' ? 'dark' : 'light';
    if (currentTheme.value === 'dark') {
        document.documentElement.classList.add('dark');
    } else {
        document.documentElement.classList.remove('dark');
    }
    document.documentElement.setAttribute('data-theme', currentTheme.value);
    localStorage.setItem('theme', currentTheme.value);
}
