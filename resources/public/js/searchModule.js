(function(global){
  var LnkLtr = global.LnkLtr;
  if(!LnkLtr){
    LnkLtr = {};
    global.LnkLtr = LnkLtr;
  }
})(this); 

LnkLtr.search ={

  init: function(){
    var self = this;
    self.bindEvents();
    $('#search').focus();
  },
  _debounce: function(func, wait, immediate) {
    var timeout;
    return function() {
      var context = this, args = arguments;
      var later = function() {
        timeout = null;
        if (!immediate) func.apply(context, args);
      };
      var callNow = immediate && !timeout;
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
      if (callNow) func.apply(context, args);
    };
  },
  bindEvents: function(){
    var searchElm = document.getElementById('search');
    var self = this;
    $(searchElm).keydown(self._debounce(function(e){
      var code = (e.keyCode ? e.keyCode : e.which);
      if(searchElm.value.length > 2) { //Enter keycode
        //make a ajax call and get url details
        var url= '/search/q',
        data = { q: searchElm.value }; 
        console.log("search query::: ", searchElm.value);
        LnkLtr.linkModule.clearList();
        LnkLtr.ajax(url, data, LnkLtr.linkModule.displaySearchList);
      }
    }, 100));
  }
};

$(document).ready(function(){
  LnkLtr.search.init();
});
