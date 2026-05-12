// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "ZephyrDfu",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "ZephyrDfu",
            targets: ["ZephyrDfuPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.3.3"),
        .package(url: "https://github.com/NordicSemiconductor/IOS-nRF-Connect-Device-Manager.git", from: "1.14.2"),
        .package(url: "https://github.com/weichsel/ZIPFoundation.git", from: "0.9.20")
    ],
    targets: [
        .target(
            name: "ZephyrDfuPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm"),
                .product(name: "iOSMcuManagerLibrary", package: "IOS-nRF-Connect-Device-Manager"),
                .product(name: "ZIPFoundation", package: "ZIPFoundation")
            ],
            path: "ios/Plugin",
            exclude: ["Info.plist"])
    ]
)
