//
//  McuMgrPackage.swift
//  nRF Connect Device Manager
//
//  Created by Dinesh Harjani on 18/1/22.
//  Copyright © 2022 Nordic Semiconductor ASA. All rights reserved.
//

import Foundation
import iOSMcuManagerLibrary
import ZIPFoundation

// MARK: - McuMgrPackage

struct McuMgrPackage {
    
    let images: [ImageManager.Image]
    
    // MARK: - Init
    
    init(from url: URL) throws {
        
        print("McuMgrPackage - Step 1")
        
        let document = UIDocument(fileURL: url)
        guard let fileType = document.fileType else {
            throw McuMgrPackage.Error.notAValidDocument
        }
        
        print("McuMgrPackage - Step 2 - " + fileType)
        
        switch UTI.from(fileType) {
        case .bin:
            self.images = try Self.extractImageFromBinFile(from: url)
        case .zip:
            self.images = try Self.extractImageFromZipFile(from: url)
        default:
            throw McuMgrPackage.Error.notAValidDocument
        }
    }
    
    // MARK: - API
    
    static func imageName(at index: Int) -> String {
        switch index {
        case 0: return "App Core"
        case 1: return "Net Core"
        default: return "Image \(index)"
        }
    }
    
    func sizeString() -> String {
        var sizeString = ""
        for (i, image) in images.enumerated() {
            sizeString += "\(image.data.count) bytes (\(Self.imageName(at: i)))"
            guard i != images.count - 1 else { continue }
            sizeString += "\n"
        }
        return sizeString
    }
    
    func hashString() throws -> String {
        var hashString = ""
        for (i, image) in images.enumerated() {
            let hash = try McuMgrImage(data: image.data).hash
            hashString += "\(hash.hexEncodedString(options: .upperCase).prefix(6)) (\(Self.imageName(at: i)))"
            guard i != images.count - 1 else { continue }
            hashString += "\n"
        }
        return hashString
    }
}

// MARK: - McuMgrPackage.Error

extension McuMgrPackage {
    
    enum Error: Swift.Error {
        case deniedAccessToScopedResource, notAValidDocument, unableToAccessCacheDirectory, manifestFileNotFound, manifestImageNotFound
    }
}

// MARK: - Private

fileprivate extension McuMgrPackage {
    
    static func extractImageFromBinFile(from url: URL) throws -> [ImageManager.Image] {
        let binData = try Data(contentsOf: url)
        return [ImageManager.Image(image: 0, data: binData)]
    }
    
    static func extractImageFromZipFile(from url: URL) throws -> [ImageManager.Image] {
        print("McuMgrPackage - Step 3")
        guard let cacheDirectoryPath = NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true).first else {
            throw McuMgrPackage.Error.unableToAccessCacheDirectory
        }
        let cacheDirectoryURL = URL(fileURLWithPath: cacheDirectoryPath, isDirectory: true)
        
        print("McuMgrPackage - Step 4")
        
        let fileManager = FileManager()
        let contentURLs = try fileManager.contentsOfDirectory(at: cacheDirectoryURL, includingPropertiesForKeys: nil, options: .skipsHiddenFiles)
        contentURLs.forEach { url in
            _ = try? fileManager.removeItem(at: url)
        }
        
        print("McuMgrPackage - Step 5")
        
        try fileManager.unzipItem(at: url, to: cacheDirectoryURL)
        let unzippedURLs = try fileManager.contentsOfDirectory(at: cacheDirectoryURL, includingPropertiesForKeys: nil, options: [])
        
        print("McuMgrPackage - Step 6")
        
        guard let dfuManifestURL = unzippedURLs.first(where: { $0.pathExtension == "json" }) else {
            throw McuMgrPackage.Error.manifestFileNotFound
        }
        
        print("McuMgrPackage - Step 6a")
        
        let manifest = try McuMgrManifest(from: dfuManifestURL)
        
        print("McuMgrPackage - Step 7")
        
        let images = try manifest.files.compactMap { manifestFile -> ImageManager.Image in
            guard let imageURL = unzippedURLs.first(where: { $0.absoluteString.contains(manifestFile.file) }) else {
                throw McuMgrPackage.Error.manifestImageNotFound
            }
            let imageData = try Data(contentsOf: imageURL)
            return (manifestFile.imageIndex, imageData)
        }
        
        print("McuMgrPackage - Step 8")
        
        try unzippedURLs.forEach { url in
            try fileManager.removeItem(at: url)
        }
        
        print("McuMgrPackage - Step 9")
        
        return images
    }
}
