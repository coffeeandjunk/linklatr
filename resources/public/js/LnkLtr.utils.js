(function(LnkLtr){
  LnkLtr.ajax = function(url, data, callback, method){
    console.log('ajax is called');
    var result;
    $.ajax({
      url: url,
      data: data || null,
      method: "Get" || method,
      success: function(response){
        if(typeof callback === 'function'){
          callback(response);
        }
      }
    });
  }

  LnkLtr.utils = LnkLtr.utils || {};

  // trims white spaces and removes slashes if present at the end of the url
  LnkLtr.utils.sanitizeUrl = function(urlStr){
    return urlStr.trim().replace(/\/$/, '');
  }

  LnkLtr.utils.isUrlValid = function(url){
    //var url = urlStr.trim();
    var urlRegex = new RegExp(/^(?:(?:https?|ftp):\/\/)(?:\S+(?::\S*)?@)?(?:(?!(?:10|127)(?:\.\d{1,3}){3})(?!(?:169\.254|192\.168)(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]-*)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff]{2,}))\.?)(?::\d{2,5})?(?:[/?#]\S*)?$/i);   
    if(url.indexOf('http') < 0 ){
      url = "http://" + url;
    }
    return urlRegex.test(url);
  };
  
  LnkLtr.utils.showError = function(errorMsg){
    console.log("Error:  " , errorMsg);
  };

  LnkLtr.utils.escapeHtml = function(str){
    return he.escape(str);
  };

  LnkLtr.utils.unescapeHtml = function(str){
    return he.unescape(str);
  };

  LnkLtr.utils.registerHandlebarHelpers = function(){
    Handlebars.registerHelper({
      unescapeHtml: function(str){
        return (LnkLtr.utils.unescapeHtml (str));
      }
    });
  };


  LnkLtr.utils.getTemplate = function(name) {
    if (Handlebars.templates === undefined || Handlebars.templates[name] === undefined) {
      $.ajax({
        url : 'templates/' + name + '.handlebars',
        success : function(data) {
          if (Handlebars.templates === undefined) {
            Handlebars.templates = {};
          }
          Handlebars.templates[name] = Handlebars.compile(data);
        },
        async : false
      });
    }
    return Handlebars.templates[name];
  };


})(LnkLtr || {})