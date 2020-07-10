# NPI Subject Headings

Returns the NPI Subject Headings in English, Norwegian Bokmål and Norwegian Nynorsk.

Requests sent to the endpoint with a language path parameter (one of "en", "nb" or "nn") return a JSON structure containing the names and ids of the different subject headings.

# Local development

````
./gradlew build test
````

# Local Lambda testing

Assumes installation of AWS CLI, Docker and SAM CLI

````
sam build
````

The function can be tested using the following commands:

````
# Get English NPI subject headings
sam local invoke "NvaNpiSubjectHeadingFunction" -e src/test/resources/english_event.json

# Get Bokmål NPI subject Headings

sam local invoke "NvaNpiSubjectHeadingFunction" -e src/test/resources/bokmaal_event.json

# Get Nynorsk NPI subject headings

sam local invoke "NvaNpiSubjectHeadingFunction" -e src/test/resources/nynorsk_event.json

# Test error for unrecognized language

sam local invoke "NvaNpiSubjectHeadingFunction" -e src/test/resources/non_existing_language_event.json

# Test error for no language path parameter

sam local invoke "NvaNpiSubjectHeadingFunction" -e src/test/resources/no_language_event.json
