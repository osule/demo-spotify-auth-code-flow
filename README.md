# spotify-auth

Demo authorization code flow using Spotify API.

# Prerequisites

1. Run the following command to install the dependencies

        lein install

2. Also install ngrok

        npm install -g ngrok

3. Sign up for an [ngrok account](https://dashboard.ngrok.com/get-started/your-authtoken) and get an auth token.
Configure the auth-token for your ngrok client.

        ngrok authtoken <your authtoken>

4. Start ngrok by running

        ngrok http 3000

5. Configure in Spotify developer dashboard the Redirect URIs callback to the forwarding address.
    
    http://<generated forwarding subdomain>.ngrok.io/callback

6. Copy `env_template` to `.env` and set the actual values.

        cp env_template .env

# Run

Run the following command to start the service

    lein run

Then visit http://localhost:3000/start in your browser.

You should be redirected to login to spotify. 

After successful login, you're redirect to a page with the response from request to fetch the access token.

The access token can be used for other requests to Spotify API resource endpoints.