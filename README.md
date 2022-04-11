# Verify-Feign Gradle Plugin
This plugin will help you to verify all rest-clients and -controllers 
in a multimodule spring project. It will check, if all feign clients have matching 
controllers with the fitting parameters and if all rest-interfaces are used by a 
client, are public endpoints or are frontend-endpoints.

This plugin currently only supports OpenFeign-clients and Spring-Boot-controllers

## Quickstart

ToDo add Quickstart guide

## Implementation

You are provided with 3 annotations

 ``@VerifyFeign``
 ``@PublicEndpoint``
 ``@FrontendEndpoint``

With these ou can mark, how to analyse the controllers and clients.

The first one marks a Feign-client and needs to be in front of an interface, which
implements client-requests. The annotation has a parameter, which describes the 
target-module, where the matching rest-controller is implemented. 

If an interface is marked with ``@VerifyFeign``, all ``@RequestLine``s will be 
checked for a matching rest-controllers. 

The rest-interfaces need to be implemented inside a ``@RestController``. Every 
interface will be tested, whether it has at least one client, which calls the 
interface. Only interfaces marked as ``@PublicEndpoint`` or as ``@FrontendEndpoint``
will be ignored.

## Gradle Tasks

This plugin will introduce 3 Gradle tasks

``verifyFeign`` checks if there are suitable spring restcontrollers for feignclient 
interfaces annotated with @VerifyFeign.

``verifyController`` checks if RestControllers are used by clients or are declared 
as Public- or Frontend-Endpoints.

``verifyApi`` checks if RestControllers are used by clients and clients have suitable 
rest-interfaces. It does this by calling ``verifyFeign`` and ``verifyController``.

All tasks can be found in the group `check`

# Demonstration

A demonstration for this plugin can be found inside exampleProject. This project 
contains a client and a server. It shows different combinations of client- and 
server-interfaces. If you call any of the 3 Gradle tasks, they should build without
any problem. 

You can try commenting out some client- or server-interfaces or delete 
a ``@PublicEndpoint`` or ``@FrontendEndpoint`` annotation and rerun the tasks. 
See how they behave. 
