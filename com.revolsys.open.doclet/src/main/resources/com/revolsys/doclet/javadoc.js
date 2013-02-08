function createAccordion(selector) {
  $(selector).each(function() {
    var active = false;
    if (location.hash) {
      if ($('a[name="' + location.hash.substring(1) + '"]', this).length > 0) {
           active = 0;
      }
    }
    if ($(this).hasClass('open')) {
      $(this).removeClass('open');
      if (active == false) {
        active = 0;
      }
    }
    $(this).accordion({
      icons : {
        header : "ui-icon-triangle-1-e",
        headerSelected : "ui-icon-triangle-1-s"
      },
      collapsible : true,
      active : active,
      autoHeight : false
    });
  });

}

$(document).ready(function() {
  $('div.htmlExample').each(function() {
    var text = String($(this).html());
    var pre = $('<pre class="prettyprint language-html"/>').text(text);
    $(this).before('<p>The following code fragment shows an example of using the API.</p>');
    $(this).before(pre);
    $('*', this).wrapAll('<div>');
    $('> div', this).prepend('<p>Use the buttons below to run the example.</p>');
    $(this).prepend('<div class="title"><a name="example">Example</a></div>');
    $(this).addClass('open');
  });

  $('div.simpleDataTable table').dataTable({
    "bInfo" : false,
    "bJQueryUI" : true,
    "bPaginate" : false,
    "bSort" : false,
    "bFilter" : false,
    "bAutoWidth": false
  });
  
  createAccordion('div.javaPackage');
  createAccordion('div.javaClass');
  createAccordion('div.javaMethod');
  createAccordion('div.htmlExample');
  prettyPrint();
  $(':button').button();
 
});
