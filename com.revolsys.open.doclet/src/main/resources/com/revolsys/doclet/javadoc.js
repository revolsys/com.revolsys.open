function activateAccordion() {
  if (location.hash) {
    var anchor = $('[name="' + location.hash.substring(1) + '"]');
    anchor.parents('.ui-accordion').accordion('activate', 0);
  } 
}

function createAccordion(selector) {
  $(selector).each(function() {
    var active;
    if ($(this).hasClass('open')) {
      active = 0;
    } else {
      active = false;
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
  activateAccordion();
  prettyPrint();
  $(':button').button();
});

$(window).bind('hashchange', activateAccordion());
