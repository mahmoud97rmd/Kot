(function () {
  const container = document.getElementById('container');

  const chart = LightweightCharts.createChart(container, {
    layout: {
      background: { type: 'solid', color: '#0b1220' },
      textColor: '#d1d4dc',
    },
    grid: {
      vertLines: { color: '#1f2a40' },
      horzLines: { color: '#1f2a40' },
    },
    timeScale: {
      borderColor: '#1f2a40',
      timeVisible: true,
      secondsVisible: false,
    },
    rightPriceScale: { borderColor: '#1f2a40' },
  });

  const series = chart.addLineSeries({
    color: '#2962FF',
    lineWidth: 2,
  });

  // Optional title using watermark
  chart.applyOptions({
    watermark: {
      visible: true,
      fontSize: 16,
      horzAlign: 'left',
      vertAlign: 'top',
      color: 'rgba(138, 160, 198, 0.35)',
      text: 'Equity Curve'
    }
  });

  function safeParseJson(x) {
    try { return JSON.parse(x); } catch (e) { return null; }
  }

  window.setEquity = function (json) {
    const arr = (typeof json === 'string') ? safeParseJson(json) : json;
    if (!arr || !Array.isArray(arr)) return;
    series.setData(arr);
    chart.timeScale().fitContent();
  };

  window.setTitle = function (t) {
    chart.applyOptions({
      watermark: {
        visible: true,
        fontSize: 16,
        horzAlign: 'left',
        vertAlign: 'top',
        color: 'rgba(138, 160, 198, 0.35)',
        text: String(t || 'Equity Curve')
      }
    });
  };

  function resize() {
    chart.applyOptions({ width: container.clientWidth, height: container.clientHeight });
  }
  window.addEventListener('resize', resize);
  resize();
})();
