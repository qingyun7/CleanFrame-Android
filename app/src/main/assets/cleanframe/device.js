/* Local Android bridge. The browser preview has no device access. */
(function () {
  'use strict';
  var native = window.AndroidTV;
  function fail(done, fallback) {
    var detail = '';
    try { detail = native && native.getLastError() || ''; } catch (e) {}
    done({ message: fallback || '暂时无法打开，请重试。', detail: detail });
  }
  function invoke(method, done, arg) {
    if (!native) { done({message:'这个操作需要在电视上使用。'}); return; }
    try {
      var ok = arg === undefined ? native[method]() : native[method](arg);
      if (ok) done(null); else fail(done);
    } catch (e) { fail(done); }
  }
  function list() {
    if (!native) return [];
    var result = JSON.parse(native.getApps());
    if (!Array.isArray(result)) throw new Error('应用列表格式不正确');
    return result.filter(function (a) { return a && a.appId && a.name; }).map(function (a) {
      var mark = window.CleanFrameAndroidMarks && window.CleanFrameAndroidMarks[a.appId];
      if (mark && mark.name) a.name = mark.name;
      return a;
    });
  }
  window.FrameDevice = {
    initialApps: function () { try { return list(); } catch (e) { return []; } },
    apps: function (done) {
      if (!native) { done({message:'应用列表需要在电视上读取。'}); return; }
      try { done(null, list()); } catch (e) { done({message:'暂时无法读取电视应用，请重试。'}); }
    },
    launchApp: function (id, done) { invoke('launchApp', done, id); },
    settings: function (done) { invoke('openSettings', done); },
    wifi: function (done) { invoke('openWifi', done); },
    inputs: function (done) { invoke('openInputs', done); },
    home: function (done) { done({message:'请按遥控器 Home 键回到主页。'}); },
    art: function (done) { done({message:'请使用主页上的壁纸欣赏按钮。'}); }
  };
}());
