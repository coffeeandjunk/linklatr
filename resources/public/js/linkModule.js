
LnkLtr.linkModule = (function(){

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
  module.resetForm = function(){
     $("#link-form").trigger('reset');
     module.resetImage();
  }
  
	module.addNewLink = function(linkObj){
		var linksContainer = $('.collection-page.grid');
    var compiledTempl = LnkLtr.utils.getTemplate('links_templ');
		//var lastRow = module.getLastRow(linksContainer);
    var link = compiledTempl(linkObj)

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

  module.displayList = function(list){
    console.log('From displayList>>> ', list);
    var linkItemTmpl = $('#link-template');
    $.each(list, function(idx, obj){
      module.addNewLink(obj);
    });
  }
	return module;
})();
