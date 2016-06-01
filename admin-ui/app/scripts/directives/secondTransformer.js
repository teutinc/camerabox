'use strict';

angular.module('photoboothAdminApp')
    .directive('cbSecondTransformer', function() {
        return {
            restrict: 'A',
            require: '?ngModel',
            link: function(scope, element, attrs, ngModel) {
                ngModel.$formatters.push(function(value) {
                    return value / 1000;
                  });

                  //format text from the user (view to model)
                  ngModel.$parsers.push(function(value) {
                    return value * 1000;
                  });
            }
        };
    });