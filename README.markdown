# prost

[![build status](https://api.travis-ci.org/kongo2002/prost.png)][travis]

*Prost* is a "beer-counter"-like app for [android][android] that keeps track of
your drinks in a bar and provides some nice statistics about your expenses and
the *"drinking performance"*.

This project is a *fun and learning experiment* and can therefore be seen as
work-in-progress. The app is written in [scala][scala] and targets the
[android][android] SDKs from version 8 to 19.


## Intentions

- learn some [android][android] app programming
- get more experience with [scala][scala]
- have fun


## Building

*Prost* can be easily built with [maven][maven] using the [android maven
plugin][plugin].


### Requirements

- JDK 1.6+
- Android SDK(s)
- Maven 3.1.1+

In order to build using the *Google Maps API V2* you need to have the *maps API*
in your local maven repository. You can either do this manually or by using the
[maven android SDK deployer][deployer]:

	git clone https://github.com/mosabua/maven-android-sdk-deployer deployer
	cd deployer
	mvn install


### Building the android package

	mvn install


### Deploy and start on a running emulator

	mvn android:deploy android:run


## Todo

Some random things or features I plan to implement at some time:

- add more commands/statistics
- add activities with charts
- add possibility to configure bars with drinks and their prices
- add time-based commands
- integrate some [gamification][gamification] features
- UI improvements, graphics ...


## Contributions

I would be happy to recieve some pull requests, bug reports or comments of any
form.  Feel free to join :-)


## Maintainer

*prost* is written by Gregor Uhlenheuer. You can reach me at
[kongo2002@gmail.com][mail] or via github.


## License

*prost* is licensed under the [Apache license][apache], Version 2.0

> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.

[android]: http://android.com
[scala]: http://scala-lang.org
[apache]: http://www.apache.org/licenses/LICENSE-2.0
[mail]: mailto:kongo2002@gmail.com
[gamification]: http://en.wikipedia.org/wiki/Gamification
[maven]: http://maven.apache.org
[plugin]: http://code.google.com/p/maven-android-plugin
[deployer]: https://github.com/mosabua/maven-android-sdk-deployer
[travis]: https://travis-ci.org/kongo2002/prost/

<!-- vim: set noet ts=4 sw=4 sts=4 tw=80: -->
