# Testing Plan

## Unit Testing

- DAO tests: verify database insert, select, update, and search behavior.
- Service tests: verify business rules such as USC email validation, price validation, status validation, duplicate conversations, and self-contact blocking.

## Black Box Testing

Test API endpoints by sending HTTP requests and checking only input/output behavior:

- `POST /register`
- `POST /login`
- `POST /items`
- `GET /items`
- `GET /search?q=`
- `POST /conversations`
- `POST /messages`

## White Box Testing

Test internal branches:

- Invalid non-USC email branch
- Invalid password branch
- Invalid price branch
- Invalid status branch
- Cannot contact own item branch
- Unauthorized conversation access branch

## Regression Testing

After each new feature, rerun authentication, item listing, search, conversation, and messaging tests.