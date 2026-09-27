// SIGEM - comportamiento común de la interfaz
document.addEventListener('DOMContentLoaded', () => {
    // Menú lateral en pantallas pequeñas
    const sidebar = document.getElementById('sidebar');
    document.querySelectorAll('[data-toggle-sidebar]').forEach(b =>
        b.addEventListener('click', () => sidebar.classList.toggle('abierto')));

    // Confirmación antes de ejecutar acciones importantes
    document.querySelectorAll('form[data-confirmar]').forEach(f =>
        f.addEventListener('submit', e => {
            if (!confirm(f.dataset.confirmar)) {
                e.preventDefault();
            }
        }));

    // Filtro rápido de tablas: <input data-filtro="#idTabla">
    document.querySelectorAll('input[data-filtro]').forEach(input => {
        const tabla = document.querySelector(input.dataset.filtro);
        if (!tabla) return;
        input.addEventListener('input', () => {
            const q = input.value.trim().toLowerCase();
            tabla.querySelectorAll('tbody tr').forEach(tr => {
                tr.style.display = tr.textContent.toLowerCase().includes(q) ? '' : 'none';
            });
        });
    });

    // Selects dependientes: <select data-depende="#clienteSelect"> con opciones data-padre="id"
    document.querySelectorAll('select[data-depende]').forEach(hijo => {
        const padre = document.querySelector(hijo.dataset.depende);
        if (!padre) return;
        const filtrar = () => {
            hijo.querySelectorAll('option[data-padre]').forEach(o => {
                const visible = !padre.value || o.dataset.padre === padre.value;
                o.hidden = !visible;
                if (!visible && o.selected) hijo.value = '';
            });
        };
        padre.addEventListener('change', filtrar);
        filtrar();
    });

    // Tooltips de Bootstrap
    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(el => new bootstrap.Tooltip(el));
});
