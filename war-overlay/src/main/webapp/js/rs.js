function resizeHeight(iframe) {
  var body = $(iframe, window.top.document).contents().find('body');
  var newHeight = body.height() + 5;

  $(iframe).height(newHeight);
}

(function($) {
  $.fn.iframeAutoHeight = function() {
    var result = this.each(function() {
      $(this).load(function() {
        resizeHeight(this);
      });
      var source = $(this).attr('src');
      $(this).attr('src', '');
      $(this).attr('src', source);
    });
    return result;
  };
}(jQuery));

function addConfirmButton(root, selector, icon, title, message) {
  $(selector, root).button({
    text : false,
    icons : {
      primary : 'ui-icon-' + icon
    }
  });
  $(selector, root).click(function() {
    var form = $(this).closest('form');
    $('<div></div>').html(message).dialog({
      title : 'title',
      buttons : {
        'Cancel' : function() {
          $(this).dialog('close');
        },
        'OK' : function() {
          $(this).dialog('close');
          $('<input>').attr({
            type : 'hidden',
            name : 'confirm',
            value : 'true'
          }).appendTo(form);
          form.submit();
        }
      }
    });
    return false;
  });

}
function refreshButtons(root) {
  addConfirmButton(
    root,
    'button.delete',
    'trash',
    'Confirm Delete',
    'Are you sure you want to delete this record?');
  $('div.actionMenu a', root).button();
  $('.button', root).button();

  $(':button', root).each(function() {
    var button = $(this);
    var classes = button.attr('class');
    var icon = undefined;
    if (classes != undefined) {
      $(classes.split(/\s+/)).each(function() {
        var cssClass = this;
        if (cssClass.indexOf("ui-auto-button-") == 0) {
          icon = cssClass.substring("ui-auto-button-".length);
        }
      });
    }
    if (icon != undefined) {
      button.button({
        icons : {
          primary : "ui-icon-" + icon
        },
        text : false
      });
    } else {
      button.button();
    }
  });
}
$(document).ready(function() {
  refreshButtons($(document));
  $('div.collapsibleBox').each(function() {
    var active;
    if ($(this).hasClass('closed')) {
      active = false;
    } else {
      active = 0;
    }
    $(this).accordion({
      icons : {
        header : "ui-icon-triangle-1-e",
        headerSelected : "ui-icon-triangle-1-s"
      },
      collapsible : true,
      active : active,
      autoHeight : false,
      change : function(event, ui) {
        $('iframe.autoHeight', ui.newContent).iframeAutoHeight();
      }
    });
  });
  $('div.jqueryTabs').tabs({
    show : function(event, ui) {
      $('> iframe.autoHeight', ui.panel).iframeAutoHeight();
    }
  });
  $('div.objectList table').dataTable({
    "bJQueryUI" : true,
    "bPaginate" : false,
    "bSort" : false
  });
});
