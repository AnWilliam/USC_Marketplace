# USC Marketplace

Java Servlet + MySQL marketplace for USC students.

## Local setup

1. Import `Marketplace` as an Eclipse Dynamic Web Project.
2. Configure Apache Tomcat 10.1 as the targeted runtime.
3. Run `Marketplace/database/schema.sql` in MySQL.
4. Run `Marketplace/database/seed.sql` in MySQL.
5. If you already created the database before image upload was added, run `Marketplace/database/add_item_images.sql`.
6. Update `Marketplace/src/main/resources/db.properties` with your MySQL username and password.
7. MySQL Connector/J is included in `Marketplace/src/main/webapp/WEB-INF/lib`.
8. Run on Tomcat and open `http://localhost:8080/Marketplace/`.

Uploaded item photos are stored in `~/usc-marketplace-uploads` and served through `/uploads/*`.

## Main endpoints

- `POST /register`
- `POST /login`
- `POST /logout`
- `GET /items`
- `GET /items?id=1`
- `POST /items`
- `GET /search?q=book`
- `POST /conversations`
- `GET /conversations`
- `POST /messages`
- `GET /messages?conversationID=1`
- `GET /profile`
- `POST /profile`
