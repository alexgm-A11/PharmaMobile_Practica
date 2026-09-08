import SwiftUI

@main
struct iOSApp: App {
    init() {
        AppModuleKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
