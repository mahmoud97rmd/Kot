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
    crosshair: {
      mode: LightweightCharts.CrosshairMode.Normal
    },
    timeScale: {
      borderColor: '#1f2a40',
      timeVisible: true,
      secondsVisible: false,
    },
    rightPriceScale: {
      borderColor: '#1f2a40'
    }
  });

  const candles = chart.addCandlestickSeries({
    upColor: '#26a69a',
    downColor: '#ef5350',
    borderUpColor: '#26a69a',
    borderDownColor: '#ef5350',
    wickUpColor: '#26a69a',
    wickDownColor: '#ef5350',
  });

  // Optional: keep last marker array
  let currentMarkers = [];

  function safeParseJson(x) {
    try { return JSON.parse(x); } catch (e) { return null; }
  }

  window.setHistory = function (json) {
    const arr = (typeof json === 'string') ? safeParseJson(json) : json;
    if (!arr || !Array.isArray(arr)) return;
    candles.setData(arr);
  };

  window.updateCandle = function (json) {
    const obj = (typeof json === 'string') ? safeParseJson(json) : json;
    if (!obj) return;
    candles.update(obj);
  };

  window.setMarkers = function (json) {
    const arr = (typeof json === 'string') ? safeParseJson(json) : json;
    if (!arr || !Array.isArray(arr)) return;
    currentMarkers = arr;
    candles.setMarkers(currentMarkers);
  };

  window.addMarker = function (json) {
    const obj = (typeof json === 'string') ? safeParseJson(json) : json;
    if (!obj) return;
    currentMarkers.push(obj);
    candles.setMarkers(currentMarkers);
  };

  // Visible range -> Android (optional)
  try {
    chart.timeScale().subscribeVisibleTimeRangeChange(function(range) {
      if (!range) return;
      if (window.Android && typeof window.Android.onVisibleRange === 'function') {
        window.Android.onVisibleRange(JSON.stringify(range));
      }
    });
  } catch (e) {}

  // Resize
  function resize() {
    chart.applyOptions({ width: container.clientWidth, height: container.clientHeight });
  }
  window.addEventListener('resize', resize);
  resize();
})();
