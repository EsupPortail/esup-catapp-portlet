catAppPortlet = function (appName, resourceURL, actionURL, containerURL, withFavs) {

    var app = angular.module(appName, ['ui.bootstrap', 'ui.sortable', 'ngSanitize']);

    app.controller('AppList2Ctrl', function ($scope, $http, FavorisService) {

        'use strict';

        $scope.alerts = [];
        $scope.applications = [];
        $scope.prefs = [];
        $scope.contUrl = encodeUrl(containerURL);
        $scope.withFavs = withFavs;
        $http
            .get(url(encodeUrl(resourceURL), 'favapps')).success(function (data) {
                angular.forEach(data.apps, function (appli) {
                    var favori = FavorisService.create(appli, false);
                    $scope.applications.push(favori);
                    $scope.prefs.push(appli.code);
                    appli.choosen = true;
                });
                angular.forEach(data.noAccessApps, function (appli) {
                    $scope.applications.push(FavorisService.create(appli, true));
                    $scope.prefs.push(appli.code);
                    appli.choosen = true;
                });
            });

        $scope.getTarget = function (appli) {
            var appTarget;
            if (appli.url.toLowerCase().indexOf($scope.contUrl) >= 0) {
                appTarget = "_self";
            } else {
                appTarget = "_blank";
            }
            return appTarget;
        }

        $scope.delFromFav = function (idx) {
            var delApp = $scope.applications[idx];
            $scope.prefs = FavorisService.delete(delApp.code, $scope.prefs);
            delApp.choosen = false;
            $scope.applications.splice(idx, 1);
            $scope.updateFavorite();
        };

        $scope.addFav = function (appli) {
            var favTodd = FavorisService.find(appli.code, $scope.prefs);
            if (!angular.isUndefined(favTodd) || favTodd != null) {
                $scope.alerts.push({type: 'danger', msg: "L'application " + appli.code + " est déjà dans les favoris"});
            } else {
                $scope.applications.push(FavorisService.create(appli, false));
                $scope.prefs.push(appli.code);
                appli.choosen = true;
                $scope.updateFavorite();
                $scope.alerts.push({type: 'success', msg: "L'application " + appli.code + " a été ajoutée aux favoris"});
            }
            setTimeout(function() {
                $(".alert").fadeOut("slow");
                $scope.alerts = [];
            }, 1000)
        };

        $scope.enableEdit = function (item) {
            item.editable = true;
        };

        $scope.disableEdit = function (item) {
            item.editable = false;
        };

        $scope.updateFavorite =  function ()  {
            $http
                .get(actUrl(encodeUrl(actionURL), 'updateFavorite', $scope.prefs.join(",")));
        };

        $http
            .get(url(encodeUrl(resourceURL), 'doms')).success(function (data) {
                $scope.domaines = data.doms;
            });

        $http
            .get(url(encodeUrl(resourceURL), 'allowedapps')).success(function (data) {
                $scope.allApplications = data.allowedapps;
            });

        $scope.sortableOptions = {
            items: '> li',
            cursor: 'move',
            helper: 'clone',
            opacity: 1,
            // called after a node is dropped
            stop: function() {
                var tmpList = [];
                angular.forEach($scope.applications, function (appli) {
                    tmpList.push(appli.code);
                });
                $scope.prefs = tmpList;
                $scope.updateFavorite();
            }
        };

    });

    app.service('FavorisService', function () {
        //save method create a new favori
        this.create = function (appli, removed) {
            var target;
            if (appli.url.toLowerCase().indexOf(encodeUrl(containerURL)) >= 0) {
                target = "_self";
            } else {
                target = "_blank";
            }
            var favori = {"code": appli.code, "title": appli.title, "caption": appli.caption, "description": appli.description, "url": appli.url, "acces": appli.activation, "state": removed, "target": target};
            return favori;
        }

        //simply search favoris list for given id
        //and returns the contact object if found
        this.find = function (code, favoris) {
            for (i in favoris) {
                if (favoris[i] == code) {
                    return favoris[i];
                }
            }
        }

        //iterate through favoris list and delete
        //contact if found
        this.delete = function (code, favoris) {
            for (i in favoris) {
                if (favoris[i] == code) {
                    favoris.splice(i, 1);
                }
            }
            return favoris;
        }
    });

    app.directive('domaine', function ($compile, $timeout) {
        return {
            restrict: "E",
            replace: true,
            scope: {
                member: '=',
                addFav: '&',
                enableEdit: '&',
                disableEdit: '&',
                prefs: '=',
                getTarget: '&'
            },
            link: function (scope, element, attrs) {
                if ((scope.member.subDomains.length > 0)) {
                    element.append("<a href='#'><i class='fa fa-caret-right pull-left' style='float:left'></i>{{member.domain.caption}}</a><ul class='sub-menu'><li ng-repeat='domaine in member.subDomains'>" +
                        "<domaine member='domaine' add-fav='addFav()' enable-edit='enableEdit()' disable-edit='disableEdit()' prefs='prefs' get-target='getTarget()'></domaine></li></ul>");
                    $compile(element.contents())(scope)
                } else {
                    element.append("<a href='#'><i class='fa fa-caret-right pull-left' style='float:left'></i>{{member.domain.caption}}</a>");
                }
                if ((scope.member.domain.applications.length > 0)) {
                    element.append("<ul class='sub-menu menudrop'><application ng-repeat='member in member.domain.applications'  " +
                                   "member='member' add-fav='addFav()' enable-edit='enableEdit()' disable-edit='disableEdit()' prefs='prefs' get-target='getTarget()'>" +
                                   "</application></ul>");
                    $compile(element.contents())(scope)
                }
            }
        }
    });

    app.directive('application', function () {
        return {
            restrict: "E",
            replace: true,
            scope: {
                    member: '=',
                    addFav: '&',
                    enableEdit: '&',
                    disableEdit: '&',
                    prefs: '=',
                    getTarget: '&'
            },
            template:"<li class='list-group-item app-item'>"+
                "<a ng-click='enable()' href='#' title='Description de l&apos;application' class='help'><i class='fa fa-lg fa-info-circle pull-left'></i></a>"+
                "<a ng-click='pushapp();' href='#' title='Ajouter l&apos;application aux favoris' ng-if='"+withFavs+"'>"+
                "<i ng-class=\"{'fa-star-o':prefs.indexOf(member.code)== -1, 'fa-star':prefs.indexOf(member.code)> -1}\" class='fa fa-lg text-warning pull-left'></i>" +
                "</a>"+
                "<a href='{{member.url}}' target='{{target}}'>{{member.title}}</a>"+
                "<div ng-show='member.editable' class='desc-appli'>"+
                "<div class='popover-title'>Description de l'application"+
                "<div class='pull-right'>"+
                "<a ng-click='disable()' href=''#'><i class='fa fa-lg fa-times'></i></a>"+
                "</div>" +
                "</div>"+
                "<div class='popover-content'><span ng-bind-html='member.description'></span>"+
                "</div>"+
                "</div>"+
                "</li>",
            link: function (scope, element, attrs) {
                scope.target = scope.getTarget()(scope.member);

                scope.pushapp = function () {
                        scope.addFav()(scope.member);
                };
                scope.enable = function () {
                    scope.enableEdit()(scope.member);
                };
                scope.disable = function () {
                    scope.disableEdit()(scope.member);
                };
            }
        }
    });
};

// ************* utils *************

function encodeUrl(requestUrl) {
    return requestUrl.
        replace(/%40/gi, '@').
        replace(/%3A/gi, ':').
        replace(/%24/g, '$').
        replace(/%2C/gi, ',');
}

//forge a portlet resource url
function url(pattern, id, p1, p2, p3, p4) {
    var pattern2 = pattern.
        replace("@@id@@", id)
        .replace("__p1__", p1)
        .replace("__p2__", p2)
        .replace("__p3__", p3)
        .replace("__p4__", p4);
    return pattern2;
}

//forge a portlet resource url
function actUrl(pattern, action, p1, p2, p3) {
    var pattern2 = pattern.
        replace("@@id@@", action)
        .replace("__p1__", p1)
        .replace("__p2__", p2)
        .replace("__p3__", p3);
    return pattern2;
}
