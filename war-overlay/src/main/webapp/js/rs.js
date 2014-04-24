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

function addConfirmButton(params) {
  rsConfirmButtons.push(params);
}

function confirmButton(root, params) {
  var selector = params['selector'];
  var icon = params['icon'];
  var title = params['title'];
  var message = params['message'];

  $(selector, root).button({
    text : false,
    icons : {
      primary : 'ui-icon-' + icon
    }
  });
  $(selector, root).click(function() {
    var form = $(this).closest('form');
    $('<div></div>').html(message).dialog({
      title : title,
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
var rsConfirmButtons = new Array();

function refreshButtons(root) {
  $(rsConfirmButtons).each(function() {
    confirmButton(root, this);
  });

  $('div.actionMenu a', root).button();

  $('.button', root).button();

  $(':button,:submit', root).each(function() {
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

function tableDraw(table, heightPercent) {
  if (heightPercent > 0) {
    var settings = table.fnSettings();
    var tableDiv = table.closest('div.table');

    var bodyDiv = tableDiv.closest('div.body');
    var bodyContent = tableDiv.closest('div.bodyContent');
    var tableScroll = $('div.dataTables_scrollBody', tableDiv);
    var scrollHeight = tableScroll.height();
    if (scrollHeight != null) {
      var collapsibleBox = tableDiv.closest('div.collapsibleBox');
      if (collapsibleBox.length > 0) {
        var otherHeight = $(bodyDiv).outerHeight(false) - bodyContent.height();
        var tableOverhead = collapsibleBox.outerHeight(false) - scrollHeight;
        var newHeight = Math.round(($(window).height() - otherHeight)
            * heightPercent)
            - tableOverhead - 15;
        if (newHeight < 50) {
          newHeight = 50;
        }
        if (newHeight != settings.oScroll.sY) {
          settings.oScroll.sY = newHeight;
        }
      }
      var tabPanel = tableDiv.closest('div.ui-tabs-panel');
      if (tabPanel.length > 0) {
        var allScroll = $('div.dataTables_scrollBody', tabPanel);
        var allScrollHeight = allScroll.height();
        var otherHeight = $(bodyDiv).outerHeight(false) - allScrollHeight;
        var availableHeight = $(window).height() - otherHeight - 5;
        var newHeight = Math.round(availableHeight * heightPercent);
        if (newHeight < 50) {
          newHeight = 50;
        }
        if (newHeight != settings.oScroll.sY) {
          settings.oScroll.sY = newHeight;
        }
      }
    }
  }
  table.fnDraw();
}

function tableShowEvents(table, heightPercent) {
  table.closest('.ui-accordion').bind('accordionchange', function(event, ui) {
    if ($(ui.panel).find(table).length > 0) {
      tableDraw(table, heightPercent);
    }
  });
  var tabs = table.closest('.ui-tabs');
  if (tabs.length > 0) {
    tabs.bind('tabsshow', function(event, ui) {
      if ($(ui.panel).find(table).length > 0) {
        tableDraw(table, heightPercent);
      }
    });

    var tab = $('.ui-tabs-panel:not(.ui-tabs-hide)', tabs);
    if (tab.find(table).length > 0) {
      tableDraw(table, heightPercent);
    }
    $(window).resize(function() {
      var tab = $('.ui-tabs-panel:not(.ui-tabs-hide)', tabs);
      if (tab.find(table).length > 0) {
        tableDraw(table, heightPercent);
      }
    });
  }
}

function validateIntegerNumber(element, value, optional, min, max) {
  return new function(value, element) {
  var text = $.trim(value);
  if (!text) {
    if (optional) {
      return true;
    } else {
      return false;
    }
  } else if (/^-?\d+$/.test(text)) {
    var number = parseInt(text);
    if (min && number < min) {
      return false;
    } else if (max && number > max) {
      return false;
    } else {
      return true;
    }
  }
  return false;
  }
}

function validateDecimalNumber(element, value, optional, min, max) {
  var text = $.trim(value);
  if (!text) {
    if (optional) {
      return true;
    } else {
      return false;
    }
  } else if (/^-?\d+(\.\d*)?$/.test(text)) {
    var number = parseFloat(text);
    if (min && number < min) {
      return false;
    } else if (max && number > max) {
      return false;
    } else {
      return true;
    }
  }
  return false;
}

$(document).ready(
  function() {
    $('html.lt-ie9').each(function() {
      document.createElement('section');
    });
    addConfirmButton({
      selector : 'button.delete',
      icon : 'trash',
      title : 'Confirm Delete',
      message : 'Are you sure you want to delete this record?'
    });
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
      create : function(event, ui) {
        var table = $.fn.dataTable.fnTables(true);
        if ( table.length > 0 ) {
          $(table).dataTable().fnAdjustColumnSizing(false);
        }
        $('> iframe.autoHeight', ui.panel).iframeAutoHeight();
      },
      activate : function(event, ui) {
        var table = $.fn.dataTable.fnTables(true);
        if ( table.length > 0 ) {
          $(table).dataTable().fnAdjustColumnSizing(false);
        }
        $('> iframe.autoHeight', ui.panel).iframeAutoHeight();
      },
      load: function (event, ui) {
        var table = $.fn.dataTable.fnTables(true);
        if (table.length > 0) {
          $(table).dataTable().fnAdjustColumnSizing(false);
        }
      },
      beforeActivate: function (event, ui) {
        window.location.hash = ui.newPanel.selector;
      },
      heightStyle: "content"
    });
 
    $(window).bind('hashchange', function() {
      var hash = window.location.hash;
      if (hash) {
        var anchor = $('a.ui-tabs-anchor[href="' + hash + '"]');
        anchor.click();
        showParentsHash(hash);
      }
      window.location.hash = hash;
    });
     
    $('div.objectList table').dataTable({
      "bJQueryUI" : true,
      "bPaginate" : false,
      "bSort" : false
    });

    $('div.simpleDataTable table').dataTable({
      "bInfo" : false,
      "bJQueryUI" : true,
      "bPaginate" : false,
      "bSort" : false,
      "bFilter" : false,
      "bAutoWidth": false
    });

    if (typeof jQuery.validator != "undefined") {
      $('div.form').each(
        function() {
          var formWrapper = this;
          var form = $('form', this);
          var validator = form.bind("invalid-form.validate", function() {
            $('div.errorContainer div.title ', formWrapper).html("The form contains errors. Update the highlighted fields to fix the errors.");
          }).validate({
            errorElement: "div",
            errorContainer : $('div.errorContainer', formWrapper),
            errorPlacement: function(error, element) {
              error.insertBefore(element);
              error.addClass('errorMessage');
            },
            highlight : function(element, errorClass, validClass) {
              $(element).closest('div.fieldComponent').addClass('invalid');
              $(element).addClass(errorClass).removeClass(validClass);
              $(element.form).find("label[for=" + element.id + "]").addClass(
                errorClass);
              $('.tempError').hide();
            },
            unhighlight : function(element, errorClass, validClass) {
              $(element).closest('div.fieldComponent').removeClass('invalid');
              $(element).removeClass(errorClass).addClass(validClass);
              $(element.form).find("label[for=" + element.id + "]").removeClass(
                errorClass);
              $('.tempError').hide();
            }
          });
          if ($(formWrapper).hasClass('formInvalid')) {
            validator.form();
          }
        }
      );

      jQuery.validator.addMethod("integer", function(value, element) {
        return validateIntegerNumber(element, value, this.optional(element));
      }, "Please enter a valid integer number.");
  
      jQuery.validator.addMethod("byte", function(value, element) {
        return validateIntegerNumber(element, value, this.optional(element), -128, 127);
      }, "Please enter a valid integer number -128 >=< 127.");
  
      jQuery.validator.addMethod("short", function(value, element) {
        return validateIntegerNumber(element, value, this.optional(element), -32768, 32767);
      }, "Please enter a valid integer number -32768 >=< 32767.");
  
      jQuery.validator.addMethod("int", function(value, element) {
        return validateIntegerNumber(element, value, this.optional(element), -2147483648, 2147483647);
      }, "Please enter a valid integer number -2147483648 >=< 2147483647.");
  
      jQuery.validator.addMethod("long", function(value, element) {
        return validateIntegerNumber(element, value, this.optional(element), -9223372036854775808, 9223372036854775807);
      }, "Please enter a valid integer number -9223372036854775808 >=< 9223372036854775807.");
  
      jQuery.validator.addMethod("number", function(value, element) {
        return validateDecimalNumber(element, value, this.optional(element));
      }, "Please enter a valid number.");
  
      jQuery.validator.addMethod("float", function(value, element) {
        return validateDecimalNumber(element, value, this.optional(element));
      }, "Please enter a valid float number.");
  
      jQuery.validator.addMethod("double", function(value, element) {
        return validateDecimalNumber(element, value, this.optional(element));
      }, "Please enter a valid double number.");
    }
    showParentsHash(window.location.hash);
  });

function clearTempErrorsOnChange() {
  $(':input').change(function() {
    console.log($(this).attr('name'));
    var errors = $('.tempError', $(this).closest('form'));
    if (errors) {
      errors.next(':input').each(function() {
        if ($(this).val()) {
          $(this).valid();
        }
        $(this).parent('div.fieldComponent').removeClass('invalid');
      });
      errors.remove();
    }
  });
}

function showParentsAClick(element) {
  var href = $(element).attr('href');
  showParentsHash(href);
  return true;
}

function showParentsHash(hash) {
  if (hash && hash.substr(0,1) == '#') {
    showParents('[id="' + hash.substr(1) + '"]');
    showParents('a[name="' + hash.substr(1) + '"]');
  }
}

function showElement(element) {
  element.removeClass('closed');
  element.show();
}

function showParents(element) {
  if (element) {
    element = $(element);
    showElement(element);
    element.parents().each(function() {
      showElement($(this));
    });
    var accordionTitle;
    if (element.hasClass('ui-accordion-header')) {
      accordionTitle = element;
      showElement(element.next());
    } else if (element.hasClass('ui-accordion-content')){
      accordionTitle = element.prev();
    }
    if (accordionTitle && !accordionTitle.hasClass('ui-accordion-header-active')) {
      accordionTitle.click();
    }
    element.parents('.ui-accordion-content').each(function() {
      showParents($(this));
    });
  }
}

function setPageId(pageId) {
  if (pageId) {
    var sideMenu =  $('.sideMenu');
    $('li > ul', sideMenu).hide(0);
    var menuLi =  $('#' + pageId + 'Menu');
    if (menuLi.length > 0) {
      menuLi.parent('.sideMenu ul').show(0);
      menuLi.addClass('selected');
      menuLi.find('ul').show(0);
    }
  }
}

function createToc() {
  var tocDiv =  $('<div class="tocMenu"/>');

  $('.sideMenu').after(tocDiv);
  tocDiv.append('<div class="title">Table of Contents</div>');
  var tocMenu = $('<ol />');
  tocDiv.append(tocMenu);
  var indices = [0];
  $(':header').each( function() {
    var menuDepth = tocMenu.parents('.tocMenu ol').length + 1;
    var headingType = $(this).get(0).tagName;
    var headingDepth = headingType.substr(1,2);
    while (headingDepth > menuDepth) {
      var li = tocMenu.last();
      if (li.length == 0) {
        li = $('<li/>');
      }
      tocMenu.append(li);
      tocMenu = $('<ol/>');
      li.append(tocMenu);
      indices.push(0);
      menuDepth++;
    }
    while(headingDepth < menuDepth) {
      tocMenu = tocMenu.parent();
      menuDepth--;
      indices.pop();
    }
    var count = indices[indices.length - 1] + 1;
    indices[indices.length - 1] = count;
    var id = $(this).attr('id');
    if (!id) {
      id = 'heading_' + indices.join('_');
      $(this).attr('id', id);
    }
    var title = $(this).attr('title');
    if (!title) {
      title = $(this).text();
    }
    var link = $('<a href="#' + id + '" onclick="showParentsAClick(this)"/>').text(title);
    $(this).prepend(indices.join('.') + '. ');
    var cssClass='';
    if (menuDepth > 3) {
      cssClass = 'class="closed"';
    }
    tocMenu.append($('<li id="tocMenu_' + indices.join('_') + '" ' + cssClass +' />').append(link));
  });
  var top = tocDiv.offset().top;
  var tocHeight = tocDiv.height();
  var resize = function() {
    var height = $(window).height();
    if (tocHeight > height - 20) {
      height -= 20;
    } else {
      height = tocHeight;
    }
    tocDiv.height( height);
    if ($(window).scrollTop() >= top) {
      var marginTop = ($(window).scrollTop() - top);
      tocDiv.css('margin-top', marginTop);
    } else {
      tocDiv.css('margin-top', 0);
    }  
  };
  $(document).resize(resize);
  $(document).scroll(resize);
  resize();
}