# Housing Finder

![GitHub Actions Status](https://github.com/rmehri01/Housing-Finder/workflows/Build/badge.svg)
[![codecov](https://codecov.io/gh/rmehri01/Housing-Finder/branch/master/graph/badge.svg?token=XAW1NC5JT9)](https://codecov.io/gh/rmehri01/Housing-Finder) <a href="https://typelevel.org/cats/"><img src="https://typelevel.org/cats/img/cats-badge.svg" height="40px" align="right" alt="Cats friendly" /></a>

## Table of Contents

* [About](#about)
  * [Robust](#robust)
  * [Extensible](#extensible)
* [Usage](#usage)
  * [Listing Routes](#listing-routes)
  * [Healthcheck Routes](#healthcheck-routes)
  * [Auth Routes](#auth-routes)
  * [Watch List Routes](#watch-list-routes)
  * [Admin Routes](#admin-routes)
* [Running Locally](#running-locally)
* [Future Improvements](#future-improvements)
* [Credit](#credit)

## About

> A robust and extensible housing finder powered by functional programming.

Initially, this was a web app that I made to help UBC students find housing off-campus in the Vancouver area. I found that visiting numerous different sites to do so is time consuming and quite repetitive, so I wanted to have one place where students can quickly see the specifications of each listing.

Since then I have worked on making the app more extensible, so while the current implementation still gets listings for UBC, it is now much easier to get listings from other places or sources. I have also added some other features that were mostly for fun or learning purposes but that I still thought were nice to have:

* User login so that users can add listings to a personal watch list.
* Caching listing results in a database to drastically reduce the time taken to retrieve them.
* Deploying the app using serverless technologies to save on costs.

### Robust

The error handling mechanics provided by functional programming allow you to only focus on errors related to business logic while letting the frameworks handle the rest. In this way, the code does not get polluted with error checks or try/catch blocks, while still maintaining a robust system in the case of other errors such as an internal server error.

Explicit encoding and handling of errors in typeclasses such as `Option` or `Either` make sure that less can go wrong, since you can safely perform operations these typeclasses, like on a `None` but would end up with a `NullPointerException` if doing so with a `null` value.

As an example, when trying to create a new user, that username may already be in use. Thus, in `user.find`, we check if some value already exists and raise a `UsernameInUse` error:

```scala
users.find(username, password).flatMap {
  case Some(_) => UsernameInUse(username).raiseError[F, JwtToken]
  case None    => ???
}
```

Then later on in the HTTP route, we try to create a new user but if it fails with `UsernameInUse`, we recover with returning `409 Conflict`:

```scala
auth
  .newUser(username, password)
  .flatMap(Created(_))
  .recoverWith {
    case UsernameInUse(u) => Conflict(u.value)
  }
```

And that's it, clean and simple!

### Extensible

Many state that functional programs are easier to comprehend and reason about, and I find this to be very true. I have found that many times throughout this project I could debug and refactor without fear of accidentally breaking something else in the system. Additionally, the code is much more self documenting, since I could quickly glance at a function's signature and immediately know what it does. For example:

```scala
trait Users[F[_]] {
  def find(username: Username, password: Password): F[Option[User]]
  def create(username: Username, password: Password): F[UserId]
}
```

Without having to worry about implementation details, it is quite easy to see what these functions do. As well, it is harder to mess up actually using these functions due to using [newtypes](https://github.com/estatico/scala-newtype), since you cannot easily pass in arguments in the wrong order, as opposed to using `String`.

Additionally, relying on traits (similar to Java interfaces) means that the implementation can be swapped out while not impacting other parts of the program, which leads to much more modularity. The technique is called [tagless final encoding](https://scalac.io/tagless-final-pattern-for-scala-code/), which naturally leads to typesafe and correct programs!

## Usage

400 bad request (refined)

422 unprocessible entity (correct syntax, wrong fields usually)

### Listing Routes

`GET /v1/listings`

params: lowerPrice, upperPrice, nums

200 ok

no request body

```json
[
    {
        "uuid": "0558b57b-c9b0-4c73-b49f-05bd61874634",
        "title": "Listing 1",
        "address": "123 Fake Street",
        "price": 2590.0,
        "description": "A very cool place!",
        "datePosted": "2020-07-25T01:14:00",
        "url": "https://www.something.com"
    },
    {
        "uuid": "0558b57b-c9b0-4c73-b49f-05bd61874635",
        "title": "Listing 2",
        "address": "12 Fake Street",
        "price": 500.25,
        "description": "Another very cool place!",
        "datePosted": "2020-07-25T01:14:00",
        "url": "https://www.somethingelse.com"
    }
]
```

### Healthcheck Routes

`GET /v1/healthcheck`

200 ok

no request

```json
{
    "redis": true,
    "postgres": true
}
```

### Auth Routes

`POST /v1/auth/users`

201 created
409 conflict `"cool name"`

```json
{
    "username": "cool name",
    "password": "password123"
}
```

```json
{
    "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1..."
}
```

`POST /v1/auth/login`

200 ok
403 forbidden (wrong username or pass)

```json
{
    "username": "cool name",
    "password": "password123"
}
```

```json
{
    "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1..."
}
```

!!applies to all after!!

403 forbidden

```json
Bearer token not found
```

!!

`POST /v1/auth/logout`

204 no content

no response body, no request body

### Watch List Routes

`GET /v1/watched`

no request

```json
[
    {
        "uuid": "0558b57b-c9b0-4c73-b49f-05bd61874634",
        "title": "Listing 2",
        "address": "12 Fake Street",
        "price": 500.25,
        "description": "Another very cool place!",
        "datePosted": "2020-07-25T01:14:00",
        "url": "https://www.somethingelse.com"
    }
]
```

`POST /v1/watched/:listingId`

201 created
409 conflict `"0558b57b-c9b0-4c73-b49f-05bd61874634"`

no response no request

`DELETE /v1/watched/:listingId`

204 no content

no response no request

### Admin Routes

`POST /v1/admin/listings`

201 created

```json
[
    {
        "title": "Listing 1",
        "address": "123 Fake Street",
        "price": 2590.0,
        "description": "A very cool place!",
        "datePosted": "2020-07-25T01:14:00",
        "url": "https://www.something.com"
    },
    {
        "title": "Listing 2",
        "address": "12 Fake Street",
        "price": 500.25,
        "description": "Another very cool place!",
        "datePosted": "2020-07-25T01:14:00",
        "url": "https://www.somethingelse.com"
    }
]
```

no response

`PUT /v1/admin/listings`

200 ok

No response body, no request body

## Running Locally

Before running the app or tests, start up Redis and Postgres by running:

```bash
docker-compose up
```

To run the app, you will need a `.env` file in the root directory. For example, the ones in [app/docker-compose.yml](app/docker-compose.yml) that are used for **testing purposes only**. Then, to run with sbt revolver:

```bash
sbt
project housing-finder-core
reStart # you can run reStart again once you make changes to reload
```

To run the tests:

```bash
sbt test
```

## Future Improvements

* Complete frontend for the app.
* Add full integration test (not just Redis and Postgres separately).
* More specific logging.
* More advanced functional techniques like MTL, optics, and streaming data.

## Credit

I was inspired to make this project after reading [Practical FP in Scala](https://leanpub.com/pfp-scala) since I wanted to practice applying the concepts in it by doing a project of my own. So big thanks to Gabriel Volpe as well as the whole Scala open source community for their help and great libraries!
