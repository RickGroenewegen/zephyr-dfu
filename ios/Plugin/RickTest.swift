import Foundation

@objc public class RickTest: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
