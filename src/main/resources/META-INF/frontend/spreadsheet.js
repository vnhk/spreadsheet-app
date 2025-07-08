import 'https://cdn.jsdelivr.net/npm/handsontable/dist/handsontable.full.min.js';

window.initSpreadsheet = function (containerElement, data) {
    const container = containerElement;
    const hyperFormulaInstance = HyperFormula.buildEmpty({
        licenseKey: 'internal-use-in-handsontable',
    });
    const hot = new Handsontable(container, {
        data: data,
        formulas: {
            engine: hyperFormulaInstance,
            sheetName: 'Sheet1',
        },
        rowHeaders: true,
        colHeaders: true,
        height: 'auto',
        autoWrapRow: true,
        autoWrapCol: true,
        contextMenu: true,
        licenseKey: 'non-commercial-and-evaluation' // for non-commercial use only
    });

    // Store instance for later use if needed
    container.hotInstance = hot;
};