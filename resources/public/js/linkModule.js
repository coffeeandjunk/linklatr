
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
		var linksContainer = $('.row.links-container');
    var linkItemTmpl = $('#link-template');
		var lastRow = module.getLastRow(linksContainer);
    var link = module.getNewLink(linkItemTmpl, linkObj);

    //reset form
    module.resetForm();

		if(lastRow.length > 0 && module.getLinkInRow(lastRow).length < 3 ){
			module.addToRow(lastRow, link);
		}else{
			module.addNewRow(link);
			//lastRow.after(addToRow(getNewRow(rowTmpl), link));
		}
	}
  module.resetImage = function(elm){
     $('#link-preview-container .link-preview').empty();
  }
  module.addImage = function(elm, link){
    module.resetImage();
    $('#link-preview-container .link-preview').append(link);
  }

  module.addPreviewLink = function(linkObj){
    var linkItemTmpl = $('#link-template'),
        linkPreviewContianer = $('#link-preview-container'),
        linkForm = $("#link-form");
    var link = module.getNewLink(linkItemTmpl, linkObj);
    linkPreviewContianer.find('.link-preview').empty().append(link); //remove previous link-preview and then add a new one
    linkForm.find('#link-name').val($.trim(linkObj.title));
    linkForm.find('#desc').val($.trim(linkObj.desc));
    linkForm.find('#image-url').val($.trim(linkObj.image_url));
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
