window.initContextMenu = function (serverRef) {
    document.querySelectorAll('.spreadsheet-header').forEach(header => {
        header.addEventListener('contextmenu', function (e) {
            e.preventDefault();
            const index = header.dataset.columnNumber;
            highlightColumn(index);
            showColumnContextMenu(e.pageX, e.pageY, index);
        });
    });

    function highlightColumn(index) {
        document.querySelectorAll('.spreadsheet-cell').forEach(cell => {
            cell.classList.remove('highlighted-column');
            cell.classList.remove('highlighted-row');
        });
        document.querySelectorAll(`.spreadsheet-cell[data-column-number='${index}']`)
            .forEach(cell => cell.classList.add('highlighted-column'));
    }

    function showColumnContextMenu(x, y, index) {
        let menu = document.getElementById('custom-context-menu');
        if (!menu) {
            menu = document.createElement('div');
            menu.id = 'custom-context-menu';
            menu.className = 'context-menu';
            document.body.appendChild(menu);
        }
        menu.innerHTML = `
            <div onclick="window.addColumnLeft()">➕ Add column left</div>
            <div onclick="window.addColumnRight()">➕ Add column right</div>
            <div onclick="window.duplicateColumn()">📄📄 Duplicate</div>
            <div onclick="window.deleteColumn()">❌ Delete column</div>
        `;
        menu.style.display = 'block';
        menu.style.left = `${x}px`;
        menu.style.top = `${y}px`;
        menu.setAttribute('data-index', index);
        menu.setAttribute('data-type', 'column');
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

    window.duplicateColumn = () => {
        const index = parseInt(document.getElementById('custom-context-menu').getAttribute('data-index'));
        serverRef.$server.duplicateColumn(index);
        hideContextMenu();
    };

    window.deleteColumn = () => {
        const index = parseInt(document.getElementById('custom-context-menu').getAttribute('data-index'));
        serverRef.$server.deleteColumn(index);
        hideContextMenu();
    };

    //row context menu
    document.querySelectorAll('.spreadsheet-row-header').forEach(header => {
        header.addEventListener('contextmenu', function (e) {
            e.preventDefault();
            const index = header.dataset.rowNumber;
            highlightRow(index);
            showRowContextMenu(e.pageX, e.pageY, index);
        });
    });

    function highlightRow(index) {
        document.querySelectorAll('.spreadsheet-cell').forEach(cell => {
            cell.classList.remove('highlighted-row');
            cell.classList.remove('highlighted-column');
        });
        document.querySelectorAll(`.spreadsheet-cell[data-row-number='${index}']`)
            .forEach(cell => cell.classList.add('highlighted-row'));
    }

    function showRowContextMenu(x, y, index) {
        let menu = document.getElementById('custom-context-menu');
        if (!menu) {
            menu = document.createElement('div');
            menu.id = 'custom-context-menu';
            menu.className = 'context-menu';
            document.body.appendChild(menu);
        }
        menu.innerHTML = `
            <div onclick="window.addRowAbove()">➕ Add row above</div>
            <div onclick="window.addRowBelow()">➕ Add row below</div>
            <div onclick="window.duplicateRow()">📄📄 Duplicate</div>
            <div onclick="window.deleteRow()">❌ Delete row</div>
        `;
        menu.style.display = 'block';
        menu.style.left = `${x}px`;
        menu.style.top = `${y}px`;
        menu.setAttribute('data-index', index);
        menu.setAttribute('data-type', 'row');
    }

    window.addRowAbove = () => {
        const index = parseInt(document.getElementById('custom-context-menu').getAttribute('data-index'));
        serverRef.$server.addRowAbove(index);
        hideContextMenu();
    };

    window.addRowBelow = () => {
        const index = parseInt(document.getElementById('custom-context-menu').getAttribute('data-index'));
        serverRef.$server.addRowBelow(index);
        hideContextMenu();
    };

    window.duplicateRow = () => {
        const index = parseInt(document.getElementById('custom-context-menu').getAttribute('data-index'));
        serverRef.$server.duplicateRow(index);
        hideContextMenu();
    };

    window.deleteRow = () => {
        const index = parseInt(document.getElementById('custom-context-menu').getAttribute('data-index'));
        serverRef.$server.deleteRow(index);
        hideContextMenu();
    };

    function hideContextMenu() {
        const menu = document.getElementById('custom-context-menu');
        if (menu) menu.style.display = 'none';
    }

    document.addEventListener('click', hideContextMenu);
};