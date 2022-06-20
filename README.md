# rick-test

Nee

## Install

```bash
npm install rick-test
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`updateFirmware(...)`](#updatefirmware)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### updateFirmware(...)

```typescript
updateFirmware(fileURL: string, deviceIdentifier: string) => Promise<{ value: string; }>
```

| Param                  | Type                |
| ---------------------- | ------------------- |
| **`fileURL`**          | <code>string</code> |
| **`deviceIdentifier`** | <code>string</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------

</docgen-api>
