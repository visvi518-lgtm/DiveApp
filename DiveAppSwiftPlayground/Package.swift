// swift-tools-version: 5.9
import PackageDescription
import AppleProductTypes

let package = Package(
    name: "DiveAppSwiftPlayground",
    platforms: [
        .iOS("17.0")
    ],
    products: [
        .iOSApplication(
            name: "DiveAppSwiftPlayground",
            targets: ["AppModule"],
            bundleIdentifier: "com.diveapp.swiftplayground",
            teamIdentifier: "",
            displayVersion: "1.0",
            bundleVersion: "1",
            accentColor: .presetColor(.blue),
            supportedDeviceFamilies: [
                .pad,
                .phone
            ],
            supportedInterfaceOrientations: [
                .portrait,
                .landscapeLeft,
                .landscapeRight
            ]
        )
    ],
    targets: [
        .executableTarget(
            name: "AppModule",
            path: "Sources"
        )
    ]
)
