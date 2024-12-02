ProjectDisplayName = JTimeSeries

JavaPackages = \
        no/geosoft/jtimeseries \
        no/geosoft/jtimeseries/util \

JavaLibraries = \
	javax.json-1.1.3.jar \
	javax.json-api-1.1.3.jar \
	justify-0.15.0.jar \

JavadocPackages = -subpackages no

include $(DEV_HOME)/tools/Make/Makefile
