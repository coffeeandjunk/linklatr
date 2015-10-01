var context = {link: {url: "https%3A%2F%2Fgithub.com%2Fring-clojure%2Fring-codec",
    user_id: 1,
      title: "This is supposed to be the url title",
			domain: "www.medium.com"
  },
  test: "njb"};

var linkModule = (function(){

	var module = {};
  var rowContainer = $('.links-container-wrapper');
	var rowTmpl = $('#row-template');
  var linksContainer = $('.links-container');


	module.getLastRow = function(linksContainer){
		return linksContainer.last();
	}

	module.addToRow = function(row, item){
		return $(row).append(item);
	}

	module.compileTemplate = function(tmpl){
		return Handlebars.compile($(tmpl).html());
	}

	// TODO implement singleton pattern
	module.getNewRow = function(rowTmpl){
		return module.compileTemplate(rowTmpl)();
	}

	// TODO implement singleton pattern
	module.getNewLink = function(linkTmpl, context){
		var tmpl = module.compileTemplate(linkTmpl);
		return tmpl(context);
	}

	module.getLinkInRow = function(row){
		return row.find('.link-item');
	}

	module.addNewRow = function(link){
		rowContainer.append(module.addToRow(module.getNewRow(rowTmpl), link));
	}

	module.addNewLink = function(link){
		var linksContainer = $('.row.links-container');
		var lastRow = module.getLastRow(linksContainer);
		if(lastRow.length > 0 && module.getLinkInRow(lastRow).length < 3 ){
			module.addToRow(lastRow, link);
		}else{
			module.addNewRow(link);
			//lastRow.after(addToRow(getNewRow(rowTmpl), link));
		}
	}

	return module;


})();

(function(global){
	var LnkLtr = global.LnkLtr;
	if(!LnkLtr){
		LnkLtr = {};
		global.LnkLtr = LnkLtr;
	}

})(this)

$(document).ready(function(){

  function isUrlPresent(obj){
   return obj.url; 
  }

	var linkItemTmpl = $('#link-template');
	$('.dummy').click(function(){
		linkModule.addNewLink(linkModule.getNewLink(linkItemTmpl, context));
	})


  /* $.ajax({
    type: "GET",
    url: '/links',
    success: function(response){
      if(response.length > 0){
        $.each(response, function(idx,obj){
          });
      }
      console.log(response);
    }
  }); */

  function isFormValid(form){
    var linkTitle = form.find('.link-label-container');
    var link = form.find('.link-container');
    if(linkTitle.children('#link-name').val().length < 1 ){
      linkTitle.addClass('has-error'); 
      return false;
    }
    linkTitle.removeClass('has-error');
    if(link.children('#link').val().length < 1 ){
      link.addClass('has-error'); 
      return false;
    }
    link.removeClass('has-error');
    return true;
  }

  var form = $('#link-form');
  $('button.link-submit').click(function() {
    if(isFormValid(form)){
      console.log(form.serialize());
      $.ajax({
        type: "POST",
        url: '/',
        data: form.serialize(),
        success: function( response ){
          if(isUrlPresent(response)){
            appendLink(response,linksContainer)
          }     
        },
        error: function( response ){

          $(".error").text(response.responseJSON.data.error).show()
            .hide(4000);
        }
      });
    }
  });
});
