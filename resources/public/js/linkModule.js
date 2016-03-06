
LnkLtr.linkModule = (function(){

	var module = {};

	module.getLastRow = function(linksContainer){
		return linksContainer.last();
	}

	module.addToRow = function(row, item){
		var elm = $(row).append(item);
    elm.trigger("linkAdded", item);
    return elm;
	}

	module.compileTemplate = function(tmpl){
		return Handlebars.compile($(tmpl).html());
	}

	// TODO implement singleton pattern
	module.getNewLink = function(linkTmpl, context){
		var tmpl = module.compileTemplate(linkTmpl);
		return tmpl(context);
	}

	module.getLinkInRow = function(row){
		return row.find('.link-item');
	}

  module.resetForm = function(){
     $("#link-form").trigger('reset');
     module.resetImage();
  }

	module.addNewLink = function(compiledTempl, linkObj){
		var linksContainer = $('.collection-page.container');
    var link = compiledTempl(linkObj);
    module.addToRow(linksContainer, link);
	}
  module.resetImage = function(elm){
     $('#link-preview-container .link-preview').empty();
  }
  module.addImage = function(elm, link){
    module.resetImage();
    $('#link-preview-container .link-preview').append(link);
  }

  module.addPreviewLink = function(linkObj){
    LnkLtr.previewModal.loadDataPreviewDialog(linkObj);
  }

  module.delteLink = function(linkelm){
    linkelm.remove();
  }

  module.getLinkTemplate = function(){
    return LnkLtr.utils.getTemplate('links_templ'); //fetches the link template and compiles it
  }

  //TODO improve the funciton, instead of re-initializing the whole container -
  // initialize only the item added
  module.initLinkTemplate = function(elm){
     $('.collection-page .dropdown').dropdown();
  }

  module.displayList = function(list){
    console.log('From displayList>>> ', list);
    $.each(list, function(idx, obj){
      var template = module.getLinkTemplate();
      module.addNewLink(template, obj);
    });
  }

  module.clearList = function(){
    $('.collection-page').empty();
  }

  module.displaySearchList = function(result){
    console.log('From displayList>>> ', result.count);
    var list = result.result;
    $.each(list, function(idx, obj){
      module.addNewLink(module.getLinkTemplate(), obj);
    });
  }
	return module;
})();
