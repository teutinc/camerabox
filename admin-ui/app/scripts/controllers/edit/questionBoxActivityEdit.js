'use strict';

angular.module('photoboothAdminApp')
    .controller('QuestionBoxActivityEditCtrl', ['$scope', '$uibModal', 'FileUploader', function ($scope, $uibModal) {
        $scope.questionToString = function (question) {
            var label = question.template.type + ': ';
            if (question.template.text) {
                label += question.template.text + ' ';
            }
            if (question.template.image) {
                label += question.template.image;
            }
            return label;
        };

        $scope.newQuestion = function () {
            var modalInstance = $uibModal.open({
                templateUrl: '/views/forms/questionEditModal.html',
                controller: 'QuestionEditModalInstanceCtrl',
                resolve: {
                    question: function () {
                        return {};
                    },
                    contents: function () {
                        return $scope.contents;
                    }
                }
            });

            modalInstance.result.then(function (question) {
                if (!$scope.activity.questions) {
                    $scope.activity.questions = [];
                }
                $scope.activity.questions.push(question);
            });
        };

        $scope.editQuestion = function (question) {
            var editedQuestion = question;
            var modalInstance = $uibModal.open({
                templateUrl: '/views/forms/questionEditModal.html',
                controller: 'QuestionEditModalInstanceCtrl',
                resolve: {
                    question: function () {
                        return angular.copy(question);
                    },
                    contents: function () {
                        return $scope.contents;
                    }
                }
            });

            modalInstance.result.then(function (question) {
                var index = $scope.activity.questions.indexOf(editedQuestion);
                if (index > -1) {
                    $scope.activity.questions[index] = question;
                }
            });
        };

        $scope.deleteQuestion = function (question) {
            var index = $scope.activity.questions.indexOf(question);
            if (index > -1) {
                $scope.activity.questions.splice(index, 1);
            }
        };
    }])
    .controller('QuestionEditModalInstanceCtrl', ['$scope', '$uibModalInstance', 'question', 'contents', function ($scope, $uibModalInstance, question, contents) {
        $scope.question = question;
        $scope.contents = contents;

        $scope.ok = function () {
            $uibModalInstance.close($scope.question);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);
