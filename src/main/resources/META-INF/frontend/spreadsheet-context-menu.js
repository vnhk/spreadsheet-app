window.initContextMenu = function (serverRef) {
    document.querySelectorAll('.spreadsheet-header').forEach(header => {
        header.addEventListener('contextmenu', function (e) {
            e.preventDefault();
            const index = header.dataset.columnIndex;
            highlightColumn(index);
            showContextMenu(e.pageX, e.pageY, index);
        });
    });

    function highlightColumn(index) {
        document.querySelectorAll('.spreadsheet-cell').forEach(cell => {
            cell.classList.remove('highlighted-column');
        });
        document.querySelectorAll(`.spreadsheet-cell[data-column-index='${index}']`)
            .forEach(cell => cell.classList.add('highlighted-column'));
    }

    function showContextMenu(x, y, index) {
        let menu = document.getElementById('custom-context-menu');
        if (!menu) {
            menu = document.createElement('div');
            menu.id = 'custom-context-menu';
            menu.className = 'context-menu';
            menu.innerHTML = `
                <div onclick="window.addColumnLeft()">➕ Add column left</div>
                <div onclick="window.addColumnRight()">➕ Add column right</div>
                <div onclick="window.deleteColumn()">❌ Delete column</div>
            `;
            document.body.appendChild(menu);
        }

        menu.style.display = 'block';
        menu.style.left = `${x}px`;
        menu.style.top = `${y}px`;
        menu.setAttribute('data-index', index);
    }

    window.addColumnLeft = () => {
        const index = parseInt(document.getElementById('custom-context-menu').getAttribute('data-index'));
        serverRef.$server.addColumnLeft(index);
        hideContextMenu();
    };

    window.addColumnRight = () => {
        const index = parseInt(document.getElementById('custom-context-menu').getAttribute('data-index'));
        serverRef.$server.addColumnRight(index);
        hideContextMenu();
    };

    window.deleteColumn = () => {
        const index = parseInt(document.getElementById('custom-context-menu').getAttribute('data-index'));
        serverRef.$server.deleteColumn(index);
        hideContextMenu();
    };

    function hideContextMenu() {
        const menu = document.getElementById('custom-context-menu');
        if (menu) menu.style.display = 'none';
    }

    document.addEventListener('click', hideContextMenu);
};