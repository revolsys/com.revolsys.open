function resizeHeight(iframe) {
  var body = $(iframe, window.top.document).contents().find('body');
  var newHeight = body[0].scrollHeight;

  if (iframe.style.height != newHeight + 'px') {
    iframe.style.height = newHeight + 'px';
  }
}

(function($) {
  $.fn.iframeAutoHeight = function(spec) {
    return this.each(function() {
      $(this).load(function() {
        resizeHeight(this);
      });
      var source = $(this).attr('src');
      $(this).attr('src', '');
      $(this).attr('src', source);
    });
  };
}(jQuery));

$(document).ready(function() {
  $("div.collapsibleBox > div > div.title").click(function() {
    var box = $(this).parent().parent();
    var content = $("> div > div.content", box);
    content.slideToggle("fast", function() {
      var container = $("> div", box);
      if (content.css("display") == "none") {
        container.removeClass("open");
        container.addClass("closed");
      } else {
        container.removeClass("closed");
        container.addClass("open");
       $("iframe.autoHeight", content).iframeAutoHeight();
      }
    });
  });
  $( "button.delete" ).button({ text:false,icons: {primary:'ui-icon-trash'} });
  $( "button.delete" ).addClass('ui-state-error');
  
  
  
  $('button.delete').click(function() {
    var form = $(this).closest('form');
    $('<div></div>')
    .html('Are you sure you want to delete this record?')
    .dialog({
      title: 'Confirm Delete',
      buttons: {
        'Cancel': function() {
          $(this).dialog("close");
        },
        'OK': function() {
          $(this).dialog("close");
          $('<input>').attr({
            type: 'hidden',
            name: 'confirm',
            value: 'true'
        }).appendTo(form);
          form.submit();
        }
      }
    });
    return false;
  });

});

