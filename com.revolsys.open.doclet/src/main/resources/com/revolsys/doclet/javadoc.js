function activateAccordion() {
  if (location.hash) {
    var anchor = $('[name="' + location.hash.substring(1) + '"]');
    anchor.parents('.ui-accordion').accordion('activate', 0);
  } 
}

$(document).ready(function() {
  $('div.simpleDataTable table').dataTable({
    "bInfo" : false,
    "bJQueryUI" : true,
    "bPaginate" : false,
    "bSort" : false,
    "bFilter" : false
  });
  
  $('div.javaMethod').accordion({
    icons : {
      header : "ui-icon-triangle-1-e",
      headerSelected : "ui-icon-triangle-1-s"
    },
    collapsible : true,
    active : 1,
    autoHeight : false
  });
  activateAccordion();
});

$(window).bind('hashchange', activateAccordion());
