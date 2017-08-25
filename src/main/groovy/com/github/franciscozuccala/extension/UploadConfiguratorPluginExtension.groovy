package com.github.franciscozuccala.extension

class UploadConfiguratorPluginExtension {
    String pomName
    String pomDescription
    String pomArtifactId
    String pomVersion
    String pomUrl

    Repository repository
    Repository snapshotRepository
    List<Developer> developers = new ArrayList<>()

    void repository(String url, Authentication authentication){
        this.repository = new Repository(url, authentication)
    }

    void snapshotRepository(String url, Authentication authentication){
        this.snapshotRepository = new Repository(url, authentication)
    }

    void developer(String id, String name, String email) {
        developers.add(new Developer(id, name, email))
    }

    Authentication authentication(String userName, String password){
        return new Authentication(userName, password)
    }

    class Repository{
        String url
        Authentication authentication

        Repository(String url, Authentication authentication){
            this.url = url
            this.authentication = authentication
        }
    }

    class Authentication{
        String userName
        String password

        Authentication(String userName, String password){
            this.userName = userName
            this.password = password
        }
    }

    class Developer{
        String id
        String name
        String email
        Developer(String id, String name, String email){
            this.id = id
            this.name = name
            this.email = email
        }
    }
}

